package cloudsearch.clients.google

import cloudsearch.clients.http.OfflineException
import cloudsearch.config.Credentials
import cloudsearch.content.ContentType
import cloudsearch.content.Files
import cloudsearch.index.IndividualFetchable
import cloudsearch.search.ContentSearchable
import cloudsearch.search.Query
import cloudsearch.storage.AccountConfig
import cloudsearch.util.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.produce
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.net.UnknownHostException

// individualMessageFetcher allows for caching the individual email fetches on search #hack
class GmailSearch(
        val account: AccountConfig,
        val messageFetcher: IndividualFetchable,
        val creds: Credentials
) : ContentSearchable {
    val logger = LoggerFactory.getLogger(javaClass.name)
    private val client = GoogleApis.gmailClient(account, creds)

    override val contentKinds = listOf(ContentType.Email) + Files.fileTypes

    override suspend fun search(query: Query, scope: CoroutineScope) = scope.produce {
        logger.debug("Searching gmail: " + query)

        try {
            val results = client
                    .users()
                    .threads()
                    .list("me")
                    .setQ(buildQuery(query))
                    .setMaxResults(20)
                    .execute()
            logger.debug("gmail fetched")

            results.threads?.distinctBy { it.id }?.toList()?.forEach {
                val msg = messageFetcher.fetch(it.id)

                if (msg != null) {
                    send(msg!!)
                }
            }
        } catch (e: UnknownHostException) {
            throw OfflineException(e)
        }
    }

    private fun buildQuery(query: Query): String {
        var q = "(${query.text})"

//        Example: {from:amy from:david}
//        Example: label:friends

        // Example: after:2004/04/16
        if (query.after != null) {
            " AND after:${formatted(query.after)}"
        }

        // Example: before:2004/04/18
        if (query.before != null) {
            q += " AND before:${formatted(query.before)}"
        }

        if (query.contentTypes != null) {
            val types = mutableListOf<String>()

            if (query.contentTypes.contains(ContentType.File) || query.contentTypes.contains(ContentType.Document)) {
                types.add("{has:attachment OR has:drive OR has:document OR has:presentation OR has:spreadsheet}")
            }

            if (query.contentTypes.contains(ContentType.Video)) {
                types.add("{has:youtube}")
            }

            if (query.contentTypes.contains(ContentType.Message)) {
                types.add("{is:chat}")
            }

            if (types.isNotEmpty()) {
                q += " AND ${types.joinToString(" OR ")}"
            }
        }

        return q
    }


    private fun formatted(ts: DateTime): String {
        return TimeFormat.yyyMmDd.print(ts)
    }
}