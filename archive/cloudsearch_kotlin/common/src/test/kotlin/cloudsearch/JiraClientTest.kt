package cloudsearch

import cloudsearch.clients.atlassian.JiraSearch
import cloudsearch.search.Query
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking

class JiraClientTest : TestCase() {

    val search = JiraSearch(Accounts.jiraAccount)

    fun testSearch() = runBlocking {
        val r = search.search(
                Query.parsed("foo involving:anyone")!!,
                this
        )

        assertNotNull(r.receiveOrNull())
    }

    fun testKeySearch() = runBlocking {
        val r = search.search(
                Query.parsed("ZOOKEEPER-2968 ZOOKEEPER-2967 involving:anyone")!!,
                this
        )

        assertNotNull(r.receiveOrNull())
    }
}