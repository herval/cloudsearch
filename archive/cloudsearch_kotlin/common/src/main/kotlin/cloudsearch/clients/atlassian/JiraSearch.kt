package cloudsearch.clients.atlassian

import cloudsearch.clients.http.Basic
import cloudsearch.clients.http.ClientBuilder
import cloudsearch.clients.http.PostEncoding
import cloudsearch.content.*
import cloudsearch.search.InvolvementType
import cloudsearch.search.Query
import cloudsearch.search.SearchOrder
import cloudsearch.search.SimpleSearchable
import cloudsearch.storage.AccountConfig
import cloudsearch.util.Parsers
import cloudsearch.util.TimeFormat
import org.joda.time.DateTime

// https://docs.atlassian.com/software/jira/docs/api/REST/7.6.1/#api/2/search-searchUsingSearchRequest
class JiraSearch(
        val account: AccountConfig
) : SimpleSearchable<JiraSearch.JiraResult>(
        client = ClientBuilder.build(
                baseUrl = account.apiServer!!,
                jsonParser = Parsers.camelCased,
                kind = JiraSearch.JiraResult::class,
                credentials = Basic(account.credentials)
        ),
        path = "/rest/api/2/search",
        contentKinds = listOf(ContentType.Task),
        postEncoding = PostEncoding.JsonBody
) {
    override fun params(query: Query): Map<String, String> {
        return mapOf(
                "jql" to jqlFor(query)
        )
    }

    private val keyRegex = Regex("""[a-zA-Z]+-[0-9]+""")

    private fun jqlFor(query: Query): String {
        var q = "(text ~ \"${query.text.replace("\"", "'")}\")"

        // pick project originalId patterns from query
        query.tokens.forEach {
            if (keyRegex.matches(it)) {
                q += " OR (key = ${it.toUpperCase()})"
            }
        }

        if (query.involving.contains(InvolvementType.Me) == true) {
            q += " AND (assignee = currentUser() OR reporter = currentUser())"
        }

        if (query.after != null) {
            q += " AND (lastModified > ${formatted(query.after)})"
        }

        if (query.before != null) {
            q += " AND (lastModified < ${formatted(query.before)})"
        }

        q += " ORDER BY updated "
        q += when (query.orderBy) {
            SearchOrder.Ascending -> "ASC"
            else -> "DESC"
        }

        return q
    }

    private fun formatted(ts: DateTime): String {
        return TimeFormat.yyyMmDd.print(ts)
    }

    override fun extract(query: Query, data: JiraSearch.JiraResult): List<Result>? {
        return data.issues.map {
            Results.task(
                    accountId = account.id,
                    accountType = account.type,
                    permalink = account.apiServer!! + "/browse/" + it.key,
                    originalId = it.key,
                    title = "[${it.key}] ${it.fields.summary}",
                    body = it.fields.description,
                    timestamp = DateTime.parse(it.fields.updated).millis,
                    labels = it.fields.labels,
                    state = stateFor(it.fields.status.name),
                    involvesMe = query.involving.contains(InvolvementType.Me)
            )
        }
    }

    private fun stateFor(status: String): TaskState {
        return when (status.toLowerCase()) {
            "open" -> TaskState.Open
            "to do" -> TaskState.InProgress
            "cancelled" -> TaskState.Cancelled
            "done" -> TaskState.Closed
            else -> TaskState.Unknown
        }
    }

    data class JiraStatus(
            val name: String
    )

    data class JiraFields(
            val description: String,
            val iconUrl: String,
            val name: String,
            val summary: String,
            val updated: String,
            val status: JiraStatus,
            val labels: List<String>
    )

    data class JiraIssue(
            val id: String,
            val key: String,
            val self: String,
            val fields: JiraFields
    )

    data class JiraResult(
            val issues: List<JiraIssue>,
            val total: Int
    )

}