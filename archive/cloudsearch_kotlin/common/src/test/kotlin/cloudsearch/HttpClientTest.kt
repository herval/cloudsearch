package cloudsearch

import cloudsearch.clients.http.HttpClient
import cloudsearch.clients.http.ResponseParser
import cloudsearch.util.Parsers
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking

class HttpClientTest : TestCase() {
    val client = HttpClient("https://api.github.com")

    fun testGet() {
        runBlocking {
            val res = client.get("/users/octocat").get.await()
            if (res != null) {
                assertEquals("HTTP/1.1 200 OK", res.statusLine.toString())

                val parser = ResponseParser(Map::class.java, Parsers.underscored)
                assertEquals(parser.extract(res)?.get("login"), "octocat")
            } else {
                fail()
            }
        }
    }
}