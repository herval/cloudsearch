//package cloudsearch.clients.trello
//
//import cloudsearch.clients.http.Basic
//import cloudsearch.clients.http.ClientBuilder
//import cloudsearch.content.*
//import cloudsearch.search.InvolvementType
//import cloudsearch.search.Query
//import cloudsearch.search.SimpleSearchable
//import cloudsearch.util.Parsers
//import org.joda.time.DateTime
//
//class TrelloSearch(
//        override val account: ConfluenceAccount
//) : SimpleSearchable<TrelloSearch.Results>(
//        ClientBuilder.build(
//                baseUrl = account.serverBase,
//                kind = TrelloSearch.Results::class,
//                jsonParser = Parsers.camelCased,
//                credentials = Basic(account.credentialsHash)
//        ),
//        path = "/rest/api/content/search",
//        contentKinds = listOf(File::class, Message::class, Post::class), // blogposts, pages, attachments and comments
//        postEncoding = null
//) {
//    override fun params(query: Query): Map<String, String> {
//        // TODO after/before
//        return mapOf(
//                "cql" to cqlFor(query),
//                "expand" to "version.when,body.view"
//        )
//    }
//
//    private fun cqlFor(query: Query): String {
//        val encoded = "\"${query.text.replace("\"", "'")}\""
//        var q = "text ~ ${encoded} OR title ~ ${encoded}"
//
//        if (query.involving?.contains(InvolvementType.Me) == true) {
//            q += " AND (creator = currentUser() OR mention = currentUser() OR contributor = currentUser())"
//        }
//
//        // TODO filter by type
//        // TODO ordering
//
//        return q
//    }
//
//    override fun extract(query: Query, data: TrelloSearch.Results): List<Result>? {
//        return data.results.map {
//            val id = it.id
//            val permalink = account.serverBase + it._links.webui
//            val ts = DateTime.parse(it.version.`when`).millis
//
//            when (it.type) {
//                "blogpost" ->
//                    Post(
//                            originalId = id,
//                            accountType = account.type(),
//                            accountId = account.id,
//                            permalink = permalink,
//                            body = it.body?.view?.value ?: "",
//                            title = it.title,
//                            timestamp = ts,
//                            involvesMe = query.involving.contains(InvolvementType.Me)
//                    )
//                "attachment" ->
//                    Files.resultFor(
//                            originalId = id,
//                            accountType = account.type(),
//                            accountId = account.id,
//                            permalink = permalink,
//                            mimeType = it.metadata.mediaType,
//                            timestamp = ts,
//                            sizeBytes = null,
//                            title = it.title,
//                            path = it._links.download,
//                            involvesMe = query.involving.contains(InvolvementType.Me)
//                    )
//                "comment" ->
//                    Message(
//                            originalId = id,
//                            accountType = account.type(),
//                            accountId = account.id,
//                            permalink = permalink,
//                            timestamp = ts,
//                            body = it.body?.view?.value ?: "",
//                            title = it.title,
//                            involvesMe = query.involving.contains(InvolvementType.Me)
//                    )
//                "page" ->
//                    Post(
//                            originalId = id,
//                            accountType = account.type(),
//                            accountId = account.id,
//                            permalink = permalink,
//                            timestamp = ts,
//                            title = it.title,
//                            body = it.body?.view?.value ?: "",
//                            involvesMe = query.involving.contains(InvolvementType.Me)
//                    )
//                else ->
//                    throw IllegalArgumentException(data.toString())
//            }
//        }
//    }
//
////    private fun stateFor(status: String): TaskState {
////        return when (status.toLowerCase()) {
////            "open" -> TaskState.Open
////            "to do" -> TaskState.InProgress
////            "cancelled" -> TaskState.Cancelled
////            "done" -> TaskState.Closed
////            else -> TaskState.Unknown
////        }
////    }
//
//
//    data class Links(
//            val webui: String,
//            val download: String
//    )
//
//    data class Version(
//            val `when`: String
//    )
//
//    data class BodyView(
//            val value: String
//    )
//
//    data class Body(
//            val view: BodyView
//    )
//
//    data class Metadata(
//            val mediaType: String
//    )
//
//    data class CResult(
//            val id: String,
//            val type: String,
//            val title: String,
//            val _links: Links,
//            val metadata: Metadata,
//            val version: Version,
//            val body: Body?
//    )
//
//    data class Results(
//            val results: List<CResult>
//    )
//
//}