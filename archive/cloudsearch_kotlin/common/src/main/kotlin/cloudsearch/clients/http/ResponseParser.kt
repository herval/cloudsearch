package cloudsearch.clients.http

import cloudsearch.auth.UnauthorizedException
import com.google.gson.Gson
import org.apache.http.HttpResponse
import org.slf4j.LoggerFactory
import java.io.InputStream

class ResponseParser<T>(val kind: Class<T>, val parser: Gson) {
    private val log = LoggerFactory.getLogger(javaClass.name)

    private fun toString(input: InputStream): String {
        return input.bufferedReader().use { it.readText() }  // defaults to UTF-8
    }

    suspend fun getAndExtract(r: Cancellable<HttpResponse>): T? {
        return extract(r.get.await())
    }

    fun extract(response: HttpResponse?): T? {
        if (response == null || notFound(response)) {
            return null
        }
        val data = toString(response.entity.content)
//        log.debug("[${response.statusLine}] ${data}")
        if(response.statusLine.statusCode == 401) {
            throw UnauthorizedException(kind.toString(), null)
        }
        return try {
            parser.fromJson<T>(data, kind)
        } catch (e: Exception) {
            log.error("Couldn't parse: ${e.message}", e)
            return null
        }
    }

    private fun notFound(response: HttpResponse): Boolean {
        return response.statusLine?.statusCode == 404
    }

}