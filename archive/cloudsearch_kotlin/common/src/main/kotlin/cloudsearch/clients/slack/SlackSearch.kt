package cloudsearch.clients.slack

import cloudsearch.clients.http.ClientBuilder
import cloudsearch.clients.http.OauthBearer
import cloudsearch.content.ContentType
import cloudsearch.content.Files
import cloudsearch.content.Result
import cloudsearch.content.Results
import cloudsearch.search.InvolvementType
import cloudsearch.search.Query
import cloudsearch.search.SearchOrder
import cloudsearch.search.SimpleSearchable
import cloudsearch.storage.AccountConfig
import cloudsearch.util.Parsers
import org.joda.time.DateTime


class SlackSearch(
        val account: AccountConfig
) : SimpleSearchable<SlackSearch.MessagesData>(
        ClientBuilder.build(
                baseUrl = "https://slack.com/api",
                kind = SlackSearch.MessagesData::class,
                jsonParser = Parsers.underscored,
                credentials = OauthBearer(account.credentials)
        ),
        path = "/search.all", // https://api.slack.com/methods/search.all/test?
        contentKinds = listOf(ContentType.Message) + Files.fileAndFolderTypes,
        postEncoding = null
) {
    override fun params(query: Query): Map<String, String> {
        // after/before not supported
        return mapOf(
                "query" to query.text,
                "sort" to if (query.orderBy != SearchOrder.Default) {
                    "timestamp"
                } else {
                    "score"
                },
                "sort_dir" to if (query.orderBy == SearchOrder.Ascending) {
                    "asc"
                } else {
                    "desc"
                }
        )
    }

    override fun extract(query: Query, data: MessagesData): List<Result>? {
        val msgs = data.messages?.matches?.distinctBy { it.permalink }?.map {
            convert(query, it)
        }.orEmpty()

        val files = data.files?.matches?.distinctBy { it.permalink }?.map {
            convert(query, it)
        }.orEmpty()

        return msgs + files
    }


    private fun convert(query: Query, msg: SlackSearch.FileMatch): Result {
        return Files.resultFor(
                originalId = msg.permalink, // slack ids dont work!
                permalink = msg.permalink,
                accountId = account.id,
                title = msg.title,
                path = msg.name,
                sizeBytes = msg.size,
                thumbnail = msg.thumb160,
                body = msg.initialComment?.comment ?: msg.name,
                accountType = account.type,
                timestamp = msg.timestamp * 1000, // lol timestamp is not in millis here?
                involvesMe = query.involving.contains(InvolvementType.Me)
        )
    }

    private fun convert(query: Query, msg: SlackSearch.MessageMatch): Result {
        fun line(username: String, m: String) = "${username}: ${m}"
        fun fullText(m: MessageMatch): String {
            return listOfNotNull(
                    msg.channel?.let { "#${it.name}" },
                    msg.previous2?.let { line(it.username, it.text) },
                    msg.previous?.let { line(it.username, it.text) },
                    msg.let { line(it.username, it.text) },
                    msg.next?.let { line(it.username, it.text) },
                    msg.next2?.let { line(it.username, it.text) }
            ).joinToString("\n")
        }

        return Results.message(
                originalId = msg.permalink, // slack ids dont work!
                permalink = msg.permalink,
                accountId = account.id,
                title = msg.text,
                body = fullText(msg),
                accountType = account.type,
                timestamp = msg.ts.toDouble().let { DateTime((it * 1000).toLong()).millis },
                involvesMe = query.involving.contains(InvolvementType.Me)
        )
    }

    data class ChannelData(
            val id: String,
            val name: String
    )

    data class MessageData(
            val type: String,
            val user: String,
            val username: String,
            val ts: String,
            val text: String,
            val iid: String,
            val permalink: String
    )

    data class MessageMatch(
            val previous: MessageData?,
            val previous2: MessageData?,
            val next: MessageData?,
            val next2: MessageData?,
            val type: String,
            val user: String,
            val username: String,
            val ts: String,
            val text: String,
            val iid: String,
            val permalink: String,
            val team: String,
            val channel: ChannelData?
    )

    data class MessageMatches(
            val matches: List<MessageMatch>
    )

    data class CommentData(
            val comment: String
    )

    data class FileMatch(
            val id: String,
            val title: String,
            val name: String,
            val permalink: String,
            val filetype: String,
            val thumb160: String,
            val size: Long,
            val timestamp: Long,
            val username: String,
            val user: String,
            val initialComment: CommentData?
    )

    data class FileMatches(
            val matches: List<FileMatch>
    )

    data class MessagesData(
            val messages: MessageMatches?,
            val files: FileMatches?
    )
}