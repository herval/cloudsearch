package cloudsearch.clients.dropbox

import cloudsearch.clients.http.ClientBuilder
import cloudsearch.clients.http.OauthBearer
import cloudsearch.clients.http.PostEncoding
import cloudsearch.content.Files
import cloudsearch.content.Result
import cloudsearch.content.Results
import cloudsearch.search.InvolvementType
import cloudsearch.search.Query
import cloudsearch.search.SimpleSearchable
import cloudsearch.storage.AccountConfig
import cloudsearch.util.Parsers
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

// https://www.dropbox.com/developers/documentation/http/documentation#files-search
class DropboxSearch(
        val account: AccountConfig
) : SimpleSearchable<DropboxSearch.SearchResults>(
        client = ClientBuilder.build(
                baseUrl = "https://api.dropboxapi.com",
                kind = SearchResults::class,
                jsonParser = Parsers.underscored,
                credentials = OauthBearer(account.credentials)
        ),
        path = "/2/files/search",
        postEncoding = PostEncoding.JsonBody,
        contentKinds = Files.fileAndFolderTypes

) {
    private val logger = LoggerFactory.getLogger(javaClass.name)


    override fun params(query: Query): Map<String, Any> {
        // after/before not supported
        return mapOf(
                "path" to "",
                "query" to query.text,
                "max_results" to query.maxResults
        )
    }

    override fun extract(query: Query, data: SearchResults): List<Result>? {
        return data.matches.map { r ->
            val m = r.metadata
            when (m.tag) {
                "file" ->
                    Files.resultFor(
                            originalId = m.id,
                            path = m.pathLower,
                            title = m.name,
                            timestamp = DateTime(m.serverModified).millis,
                            permalink = "https://www.dropbox.com/home${m.pathLower}?preview=${m.name}",
                            sizeBytes = m.size,
                            accountId = account.id,
                            accountType = account.type,
                            involvesMe = query.involving.contains(InvolvementType.Me)
                    )
                "folder" ->
                    Results.folder(
                            originalId = m.id,
                            path = m.pathLower,
                            title = m.name,
                            timestamp = DateTime(m.serverModified).millis,
                            permalink = "https://www.dropbox.com/home${m.pathLower}?preview=${m.name}",
                            accountId = account.id,
                            thumbnail = null,
                            accountType = account.type,
                            involvesMe = query.involving.contains(InvolvementType.Me)
                    )
                else -> throw IllegalArgumentException(m.tag)
            }
        }
    }

    data class Metadata(
            @SerializedName(".tag") val tag: String,
            val name: String,
            val id: String,
            val size: Long,
            val pathLower: String,
            val rev: String,
            val serverModified: String
    )

    data class Match(
            val metadata: Metadata
    )

    data class SearchResults(
            val matches: List<Match>
    )
}
