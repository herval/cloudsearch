package cloudsearch.clients.google

import cloudsearch.auth.UnauthorizedException
import cloudsearch.clients.http.ResponseParser
import cloudsearch.content.ContentType
import cloudsearch.content.Result
import cloudsearch.content.Results
import cloudsearch.index.BulkIndexable
import cloudsearch.index.IndividualFetchable
import cloudsearch.storage.AccountConfig
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.produce
import org.joda.time.DateTime
import org.joda.time.Duration
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

// fetch calendars - currently support only the ones the user has maked as active and not hidden on their calendar list
// https://developers.google.com/google-apps/calendar/v3/reference/calendarList#resource
class GoogleCalendarFetch(
        val account: AccountConfig
) : BulkIndexable, IndividualFetchable {
    override val updateInterval: Duration = Duration.standardMinutes(5)
    override val contentKinds = listOf(ContentType.Calendar)
    val logger = LoggerFactory.getLogger(javaClass.name)
    val client = Google.client(account)
    val calendarParser = ResponseParser(EntryData::class.java, client.parser)
    val listParser = ResponseParser(ListData::class.java, client.parser)

    suspend override fun fetch(contentId: String): Result? {
        return convert(
                calendarParser.getAndExtract(
                        client.get("/calendar/v3/calendars/${contentId}")
                )
        )
    }

    suspend override fun fetchAll(
            watermark: String?, // there's no watermark for fetching calendar lists
            onDone: (String?) -> Unit,
            scope: CoroutineScope
    ) = scope.produce {

        try {
            logger.debug("Fetching calendars: ${account.id} ${watermark}")

            val calendars = calendarList()
            if (calendars != null) {
                calendars.items.forEach {
                    val c = convert(it)
                    if (c != null && !it.hidden && it.selected) { // ignore hidden or !selected calendars
                        send(c!!)
                    }
                }
            }
            onDone(null)
            logger.debug("Fetched calendar lists for ${account.id}")
        } catch (e: GoogleJsonResponseException) {
            if (e.statusCode == 401) {
                throw UnauthorizedException(account.username, e)
            }
        }
    }


    private fun convert(calendar: EntryData?): Result? {
        if (calendar == null) {
            return null
        }

        return Results.calendar(
                originalId = calendar.id,
                title = calendar.summary,
                body = calendar.description,
                permalink = null,
                accountId = account.id,
                timestamp = DateTime.now().millis,
                accountType = account.type,
                involvesMe = true // TODO others' calendars?
        )
    }

    private suspend fun calendarList(): ListData? {
        return listParser.getAndExtract(
                client.get("/calendar/v3/users/me/calendarList",
                        params = mapOf(
                                "showHidden" to "false"
                        )
                )
        )
    }


    data class ListData(
            val items: List<EntryData>
    )

    // https://developers.google.com/google-apps/calendar/v3/reference/calendarList#resource
    data class EntryData(
            val id: String,
            val summary: String,
            val hidden: Boolean,
            val selected: Boolean,
            val description: String,
            val timeZone: String,
            val location: String
    )
}
