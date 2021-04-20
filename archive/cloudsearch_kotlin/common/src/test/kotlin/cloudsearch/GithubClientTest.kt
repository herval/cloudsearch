package cloudsearch

import cloudsearch.clients.github.GithubIssueSearch
import cloudsearch.search.Query
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking

class GithubClientTest : TestCase() {
    val issues = GithubIssueSearch(
            Accounts.githubAccount
    )

    fun testSearchIssues() = runBlocking {
        val r = issues.search(
                Query.parsed("foo")!!,
                this
        )

        assertNotNull(r.receiveOrNull())
    }
}