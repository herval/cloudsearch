package cloudsearch.search

import cloudsearch.clients.http.*
import cloudsearch.content.ContentType
import cloudsearch.content.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.isActive
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext


abstract class SimpleSearchable<T : Any>(
        val client: Client<T>,
        val path: String,
        val postEncoding: PostEncoding?, // null to use GET
        override val contentKinds: List<ContentType>
) : ContentSearchable {
    private val log = LoggerFactory.getLogger(javaClass.name)

    abstract fun params(query: Query): Map<String, Any>
    abstract fun extract(query: Query, data: T): List<Result>?

    suspend override fun search(query: Query, scope: CoroutineScope): ReceiveChannel<Result> = scope.produce {
        val data = if (postEncoding == null) {
            client.get(
                    path,
                    params = params(query),
                    scope = this
            )
        } else {
            client.post(
                    path,
                    params = params(query),
                    encoding = postEncoding,
                    scope = this
            )
        }

        if (data != null) {
            val extracted = extract(query, data)
            if (extracted == null) {
                log.debug("Couldn't extract: ${data}")
            }

            extracted?.forEach {
                if (isActive) {
                    //                println(it)
                    send(it)
                }

            }
        } else {
            log.debug("Data was null?")
            throw IllegalStateException("Couldn't fetch data for ${query}")
        }
    }

}