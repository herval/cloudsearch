package cloudsearch.clients.http

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.newFixedThreadPoolContext
import org.apache.http.HttpResponse
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.entity.StringEntity
import org.apache.http.impl.nio.client.HttpAsyncClients
import org.apache.http.message.BasicNameValuePair
import org.slf4j.LoggerFactory
import java.net.NoRouteToHostException
import java.net.UnknownHostException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

enum class PostEncoding {
    JsonBody,
    FormUrlEncoded
}

class HttpClient(
        val baseUrl: String,
        val parser: Gson = Gson(),
        val credentials: Credentials? = null
) {
    private val log = LoggerFactory.getLogger(javaClass.name)

    companion object {
        // one client per app is enough
        val ioPool = CoroutineScope(newFixedThreadPoolContext(2, "io-pool"))
        val client = HttpAsyncClients.createDefault()

        init {
            client.start()
        }
    }

    fun post(
            path: String,
            headers: Map<String, String> = emptyMap(),
            params: Map<String, Any> = emptyMap(),
            encoding: PostEncoding?
    ): Cancellable<HttpResponse> {
        val contentType = if (encoding == PostEncoding.JsonBody) {
            "Content-Type" to "application/json"
        } else if (params.isNotEmpty()) {
            "Content-Type" to "application/x-www-form-urlencoded"
        } else {
            null // don't set any new header
        }

        val head = contentType?.let { headers.plus(it) } ?: headers

        log.debug("POST ${baseUrl + path} / ${params} / ${head}")
        return exec(
                HttpPost(baseUrl + path).apply {
                    entity = when (encoding) {
                        PostEncoding.JsonBody ->
                            StringEntity(
                                    parser.toJson(params)
                            )
                        PostEncoding.FormUrlEncoded ->
                            UrlEncodedFormEntity(
                                    params.map { BasicNameValuePair(it.key, it.value.toString()) }
                            )
                        else -> null
                    }
                },
                head
        )
    }

    fun get(
            path: String,
            headers: Map<String, String> = emptyMap(),
            params: Map<String, Any> = emptyMap()
    ): Cancellable<HttpResponse> {
        val encoded = ClientBuilder.urlEncode(params)
        val url = "${baseUrl}${path}" +
                if (params.isNotEmpty()) {
                    "?${encoded}"
                } else {
                    ""
                }

        log.debug("GET ${url} / ${headers}")
        return exec(HttpGet(url), headers)
    }

    private fun exec(
            request: HttpRequestBase,
            headers: Map<String, String> = emptyMap()
    ): Cancellable<HttpResponse> {
        var future: Future<HttpResponse>? = null
        return object : Cancellable<HttpResponse> {
            override val get: Deferred<HttpResponse?> = ioPool.async {
                request.addHeader("Accept", "application/json")

                when (credentials) {
                    is OauthBearer ->
                        request.addHeader("Authorization", "Bearer ${credentials.token}")
                    is OauthToken ->
                        request.addHeader("Authorization", "Token ${credentials.token}")
                    is Basic -> {
                        request.addHeader("Authorization", "Basic ${credentials.base64}")
                    }
                }

                headers.forEach {
                    request.addHeader(it.key, it.value)
                }

                future = client.execute(request, null)

                try {
                    future?.get()
                } catch (e: ExecutionException) {
                    throw OfflineException(e)
                } catch (e: NoRouteToHostException) {
                    throw OfflineException(e)
                } catch (e: UnknownHostException) {
                    throw OfflineException(e)
                }

            }

            override fun cancel() {
                try {
                    log.debug("Cancelling request ${request}")
                    future?.cancel(false)
                    request.abort()
                } catch (e: Exception) {
                    log.error(e.message, e)
                }
            }
        }
    }
}
