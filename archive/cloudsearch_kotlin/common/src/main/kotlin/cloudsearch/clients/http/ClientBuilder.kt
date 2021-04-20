package cloudsearch.clients.http

import com.google.gson.Gson
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

object ClientBuilder {
    private val log = LoggerFactory.getLogger(javaClass.name)

    fun <T : Any> build(
            baseUrl: String,
            jsonParser: Gson,
            kind: KClass<T>,
            credentials: Credentials?
    ): Client<T> {
        return Client(
                HttpClient(baseUrl, jsonParser, credentials),
                ResponseParser(kind.java, jsonParser)
        )
    }

    fun <T : Any> build(
            client: HttpClient,
            kind: KClass<T>
    ): Client<T> {
        return Client(
                client,
                ResponseParser(kind.java, client.parser)
        )
    }


    fun urlEncode(params: Map<String, Any>): String {
        return URLEncodedUtils.format(
                params.map { BasicNameValuePair(it.key, it.value.toString()) },
                "UTF-8"
        )
    }

}
