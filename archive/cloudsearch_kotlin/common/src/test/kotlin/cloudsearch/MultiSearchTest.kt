package cloudsearch

import cloudsearch.clients.atlassian.ConfluenceSearch
import cloudsearch.clients.atlassian.JiraSearch
import cloudsearch.content.Result
import cloudsearch.filter.Dedup
import cloudsearch.search.MultiSearch
import cloudsearch.search.Query
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking

class MultiSearchTest : TestCase() {

    val search = MultiSearch()

    fun testSearch() = runBlocking {
        val res = mutableListOf<Result>()
        val q = Query.parsed("foo")!!
        search.search(
                accounts = listOf(
                        Accounts.jiraAccount,
                        Accounts.confluenceAccount
                ),
                searchables = listOf(
                        JiraSearch(Accounts.jiraAccount),
                        ConfluenceSearch(Accounts.confluenceAccount)
                ),
                filters = listOf(
                        Dedup(q)
                ),
                query = q,
                onResult = {
                    res.add(it)
                },
                onFinish = { _, _ -> },
                scope = this
        )

        assert(res.isNotEmpty())
    }
}