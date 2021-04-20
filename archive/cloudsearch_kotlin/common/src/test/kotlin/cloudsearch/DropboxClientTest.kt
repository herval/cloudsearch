package cloudsearch

import cloudsearch.clients.dropbox.DropboxSearch
import cloudsearch.search.Query
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking

class DropboxClientTest : TestCase() {
    val search = DropboxSearch(
            Accounts.dropboxAccount
    )

    fun testSearchIssues() = runBlocking {
        val r = search.search(
                Query.parsed("readme")!!,
                this
        )

        assertNotNull(r.receiveOrNull())
    }
}