package cloudsearch.clients.http

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class Client<T>(
        private val client: HttpClient,
        private val parser: ResponseParser<T>
) {

    companion object {
        val log = LoggerFactory.getLogger(javaClass.name)

        private fun <T> withCancelling(
                c: Cancellable<T>,
                context: ProducerScope<*>
        ): Cancellable<T> {
            // launch a monitor to check if the pipe was cancelled
            context.launch {
                //            log.debug("Monitoring for cancel...")
                while (context.isActive) {
                    delay(200)
                }

                // context no longer active, kill c if it is
                if (c.get.isActive) {
                    log.debug("Process still active! Killing it")
                    c.cancel()
                }
            }

            return c
        }
    }

    suspend fun get(
            path: String,
            params: Map<String, Any>,
            scope: ProducerScope<*>
    ): T? {
        val c = client.get(
                path,
                params = params
        )

        return parser.getAndExtract(
                scope.let {
                    withCancelling(
                            c, scope
                    )
                } ?: c
        )
    }

    suspend fun post(
            path: String,
            params: Map<String, Any>,
            encoding: PostEncoding?,
            scope: ProducerScope<*>?
    ): T? {
        val c = client.post(
                path,
                params = params,
                encoding = encoding
        )

        return parser.getAndExtract(
                scope?.let {
                    withCancelling(c, scope)
                } ?: c
        )
    }

}