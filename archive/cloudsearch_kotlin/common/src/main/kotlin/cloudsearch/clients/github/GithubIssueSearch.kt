package cloudsearch.clients.github

import cloudsearch.content.ContentType
import cloudsearch.content.Result
import cloudsearch.content.Results
import cloudsearch.content.TaskState
import cloudsearch.search.InvolvementType
import cloudsearch.search.Query
import cloudsearch.storage.AccountConfig
import org.joda.time.DateTime


class GithubIssueSearch(
        val account: AccountConfig
) : GithubSearch<GithubIssueSearch.GithubIssuesData>(
        jsonKind = GithubIssuesData::class,
        path = "/search/issues",
        account = account,
        contentKinds = listOf(ContentType.Task)
) {

    override fun params(query: Query): Map<String, String> {
        return super.params(query).plus(
                mapOf(
                        "q" to query.text + if (query.involving.contains(InvolvementType.Anyone) == true) {
                            ""
                        } else {
                            " involves:${account.username}"
                        }
                ) // TODO since: YYYY-MM-DDTHH:MM:SSZ
        )
    }

    override fun extract(query: Query, data: GithubIssuesData): List<Result>? {
        return data.items?.map {
            Results.task(
                    accountType = account.type,
                    accountId = account.id,
                    body = it.body,
                    title = it.title,
                    timestamp = DateTime.parse(it.updatedAt ?: it.createdAt).millis,
                    originalId = it.id.toString(),
                    permalink = it.htmlUrl,
                    state = stateFor(it.state),
                    labels = it.labels,
                    involvesMe = query.involving.contains(InvolvementType.Me)
            )
        }
    }

    private fun stateFor(state: String): TaskState {
        return when (state) {
            "open" -> TaskState.Open
            "closed" -> TaskState.Closed
            else -> throw IllegalArgumentException(state)
        }
    }

    data class GithubIssueData(
            val url: String,
            val htmlUrl: String,
            val title: String,
            val body: String,
            val state: String, // open or closed
            val labels: List<String>?,
            val authorAssociation: String,
            val id: Int,
            val updatedAt: String?,
            val createdAt: String?
    )

    data class GithubIssuesData(
            val items: List<GithubIssueData>?
    )
}
