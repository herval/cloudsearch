package cloudsearch.clients.atlassian

import cloudsearch.clients.http.Basic
import cloudsearch.clients.http.ClientBuilder
import cloudsearch.content.ContentType
import cloudsearch.content.Files
import cloudsearch.content.Result
import cloudsearch.content.Results
import cloudsearch.search.InvolvementType
import cloudsearch.search.Query
import cloudsearch.search.SimpleSearchable
import cloudsearch.storage.AccountConfig
import cloudsearch.util.Parsers
import cloudsearch.util.TimeFormat
import org.joda.time.DateTime

// https://developer.atlassian.com/server/confluence/advanced-searching-using-cql/
class ConfluenceSearch(
        val account: AccountConfig
) : SimpleSearchable<ConfluenceSearch.CResults>(
        client = ClientBuilder.build(
                baseUrl = account.apiServer!!,
                jsonParser = Parsers.camelCased,
                kind = ConfluenceSearch.CResults::class,
                credentials = Basic(account.credentials)
        ),
        path = "/rest/api/content/search",
        contentKinds = listOf(ContentType.Message, ContentType.Post) + Files.fileTypes, // blogposts, pages, attachments and comments
        postEncoding = null
) {
    override fun params(query: Query): Map<String, String> {
        return mapOf(
                "cql" to cqlFor(query),
                "expand" to "version.when,body.view"
        )
    }

    private fun cqlFor(query: Query): String {
        val encoded = "\"${query.text.replace("\"", "'")}\""
        var q = "text ~ ${encoded} OR title ~ ${encoded}"

        if (query.involving.contains(InvolvementType.Me) == true) {
            q += " AND (creator = currentUser() OR mention = currentUser() OR contributor = currentUser())"
        }

        if (query.after != null) {
            q += " AND (lastModified > ${formatted(query.after)})"
        }

        if (query.before != null) {
            q += " AND (lastModified < ${formatted(query.before)})"
        }

        return q
    }

    private fun formatted(ts: DateTime): String {
        return TimeFormat.yyyMmDd.print(ts)
    }

    override fun extract(query: Query, data: ConfluenceSearch.CResults): List<Result>? {
        return data.results.map {
            val id = it.id
            val permalink = account.apiServer!! + it._links.webui
            val ts = DateTime.parse(it.version.`when`).millis

            when (it.type) {
                "blogpost" ->
                    Results.post(
                            originalId = id,
                            accountType = account.type,
                            accountId = account.id,
                            permalink = permalink,
                            body = it.body?.view?.value ?: "",
                            title = it.title,
                            timestamp = ts,
                            involvesMe = query.involving.contains(InvolvementType.Me)
                    )
                "attachment" ->
                    Files.resultFor(
                            originalId = id,
                            accountType = account.type,
                            accountId = account.id,
                            permalink = permalink,
                            mimeType = it.metadata.mediaType,
                            timestamp = ts,
                            sizeBytes = null, // TODO fetch this?
                            title = it.title,
                            path = it._links.download,
                            involvesMe = query.involving.contains(InvolvementType.Me)
                    )
                "comment" ->
                    Results.message(
                            originalId = id,
                            accountType = account.type,
                            accountId = account.id,
                            permalink = permalink,
                            timestamp = ts,
                            body = it.body?.view?.value ?: "",
                            title = it.title,
                            involvesMe = query.involving.contains(InvolvementType.Me)
                    )
                "page" ->
                    Results.post(
                            originalId = id,
                            accountType = account.type,
                            accountId = account.id,
                            permalink = permalink,
                            timestamp = ts,
                            title = it.title,
                            body = it.body?.view?.value ?: "",
                            involvesMe = query.involving.contains(InvolvementType.Me)
                    )
                else ->
                    throw IllegalArgumentException(data.toString())
            }
        }
    }

//    private fun stateFor(status: String): TaskState {
//        return when (status.toLowerCase()) {
//            "open" -> TaskState.Open
//            "to do" -> TaskState.InProgress
//            "cancelled" -> TaskState.Cancelled
//            "done" -> TaskState.Closed
//            else -> TaskState.Unknown
//        }
//    }


    data class Links(
            val webui: String,
            val download: String
    )

    data class Version(
            val `when`: String
    )

    data class BodyView(
            val value: String
    )

    data class Body(
            val view: BodyView
    )

    data class Metadata(
            val mediaType: String
    )

    data class CResult(
            val id: String,
            val type: String,
            val title: String,
            val _links: Links,
            val metadata: Metadata,
            val version: Version,
            val body: Body?
    )

    data class CResults(
            val results: List<CResult>
    )

}