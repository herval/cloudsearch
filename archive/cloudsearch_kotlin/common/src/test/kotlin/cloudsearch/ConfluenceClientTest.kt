package cloudsearch

import cloudsearch.clients.atlassian.ConfluenceSearch
import cloudsearch.search.Query
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking

class ConfluenceClientTest : TestCase() {

    val search = ConfluenceSearch(Accounts.confluenceAccount)

    fun testSearch() = runBlocking {
        val r = search.search(
                Query.parsed("foo")!!,
                this
        )

        assertNotNull(r.receiveOrNull())
    }
}