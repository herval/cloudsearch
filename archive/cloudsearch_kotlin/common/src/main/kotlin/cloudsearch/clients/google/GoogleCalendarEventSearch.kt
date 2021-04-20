package cloudsearch.clients.google

import cloudsearch.cache.ResultsCache
import cloudsearch.clients.http.ClientBuilder
import cloudsearch.content.ContentType
import cloudsearch.content.Result
import cloudsearch.content.Results
import cloudsearch.search.ContentSearchable
import cloudsearch.search.InvolvementType
import cloudsearch.search.Query
import cloudsearch.search.SearchOrder
import cloudsearch.storage.AccountConfig
import cloudsearch.util.TimeFormat
import cloudsearch.util.TimeFormat.yyyyMmDdTHhMmSsZ
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.slf4j.LoggerFactory

// https://developers.google.com/google-apps/calendar/v3/reference/events/list
class GoogleCalendarEventSearch(
        val account: AccountConfig,
        val calendarsStorage: ResultsCache
) : ContentSearchable {
    override val contentKinds = listOf(ContentType.Event)
    val logger = LoggerFactory.getLogger(javaClass.name)

    val client = ClientBuilder.build(
            Google.client(account),
            ListData::class
    )

    suspend override fun search(query: Query, scope: CoroutineScope) = scope.produce<Result> {
        // for each active calendar, search events
        val cals = mutableListOf<Result>()
        calendarsStorage.search(
                Query.parsed("")!!.copy(
                        accounts = listOf(account),
                        contentTypes = listOf(ContentType.Calendar)
                ),
                scope
        ).consumeEach { cal ->
            cals.add(cal)
        }

        // unless after or before are specified, search first for future events,
        // then past (to avoid getting clogged with past events on any given calendar
        if (query.after == null && query.before == null) {
            val cutoff = DateTime.now().minusMinutes(10)
            searchCals(cals, query.copy(after = cutoff), this)
            searchCals(cals, query.copy(before = cutoff), this)
        } else {
            searchCals(cals, query, this)
        }
    }

    private suspend fun searchCals(cals: MutableList<Result>, query: Query, scope: ProducerScope<Result>) {
        cals.forEach { cal ->
            val r = searchCalendar(cal.originalId, query, scope)
            if (r?.items == null) {
                logger.debug("No results found for ${cal.id}")
            }
            r?.items?.forEach {
                convert(query, cal, it).let {
                    scope.send(it)
                }
            }
        }
    }

    private fun convert(query: Query, calendar: Result, data: EntryData): Result {
        return Results.event(
                originalId = data.id,
                title = data.summary,
                accountId = account.id,
                body = data.description,
                calendarEmail = account.username,
                createdAt = data.created?.let { TimeFormat.yyyyMmDdTHhMmSsZ.parseMillis(it) } ?: 0,
                startAt = parse(data.start),
                endAt = parse(data.end),
                location = data.location,
                timezone = data.timeZone,
                calendarId = calendar.id,
                accountType = account.type,
                organizer = data.organizer?.email,
                attendees = data.attendees?.map { it.email } ?: emptyList(),
                permalink = data.hangoutLink ?: data.htmlLink
                ?: "https://www.google.com/calendar/event?tmeid=${data.id}&ctz=${data.timeZone
                        ?: "Etc/UTC"}&tmsrc=${account.username}",
                involvesMe = query.involving.contains(InvolvementType.Me)
                // TODO guests?
        )
    }

    private fun parse(date: TimeWithTs?): Long? {
        return date?.dateTime?.let {
            ISODateTimeFormat.dateTimeParser().parseDateTime(it).millis
        }
    }

    private suspend fun searchCalendar(id: String, query: Query, scope: ProducerScope<*>): ListData? {
        // TODO involving
        val params = mutableMapOf<String, Any>(
                "q" to query.text,
                "alwaysIncludeEmail" to true
        )
        if (query.orderBy == SearchOrder.Ascending) {
            params.put("orderBy", "updated")
        }
        if (query.after != null) {
            params.put("timeMin", yyyyMmDdTHhMmSsZ.print(query.after))
        }
        if (query.before != null) {
            params.put("timeMax", yyyyMmDdTHhMmSsZ.print(query.before))
        }

        return client.get(
                "/calendar/v3/calendars/${id}/events",
                params = params,
                scope = scope
        )
    }

    data class TimeWithTs(
            val dateTime: String?
    )


    data class Person(
            val email: String
    )

    data class EntryData(
            val id: String,
            val status: String, // confirmed, tentative, cancelled // TODO save this status
            val created: String?,
            val updated: String?,
            val description: String?,
            val summary: String,
            val start: TimeWithTs,
            val end: TimeWithTs,
            val htmlLink: String?,
            val hangoutLink: String?,
            val organizer: Person?,
            val attendees: List<Person>?,
            val location: String?,
            val timeZone: String?
    )

    data class ListData(
            val items: List<EntryData>
    )
}
