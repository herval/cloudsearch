package cloudsearch.clients.google

import cloudsearch.auth.UnauthorizedException
import cloudsearch.content.*
import cloudsearch.index.BulkIndexable
import cloudsearch.index.IndividualFetchable
import cloudsearch.search.ContentHolder
import cloudsearch.storage.AccountConfig
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.people.v1.PeopleService
import com.google.api.services.people.v1.model.ListConnectionsResponse
import com.google.api.services.people.v1.model.Person
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.produce
import org.joda.time.Duration
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext


class GoogleContactsFetch(
        val account: AccountConfig
) : BulkIndexable, IndividualFetchable, ContentHolder {
    val logger = LoggerFactory.getLogger(javaClass.name)
    override val contentKinds = listOf(ContentType.Contact)
    override val updateInterval: Duration = Duration.standardMinutes(1)

    suspend override fun fetch(id: String): Result? {
        val res = contacts().setResourceName(id).execute()
        val updated = res.connections?.firstOrNull()
        logger.debug("Fetched updated details for ${id}: ${updated}")

        return if (updated != null) {
            convert(updated)
        } else {
            null
        }
    }

    suspend fun me(): Person? {
        return client.people()
                .get("people/me")
                .setPersonFields("emailAddresses")
                .execute()
    }

    private val userFields = "names,nicknames,addresses,biographies,birthdays,emailAddresses,interests,photos,urls,phoneNumbers,organizations"

    private suspend fun contacts(): PeopleService.People.Connections.List {
        return client.people()
                .connections()
                .list("people/me")
                .setPageSize(500)
                .setPersonFields(userFields)
    }

    private suspend fun fetchPaged(page: String?, watermark: String?): ListConnectionsResponse? {
        logger.debug("Fetching page: ${page} ${watermark}")
        val contacts = contacts().setRequestSyncToken(true)

        if (watermark != null) {
            contacts.setSyncToken(watermark)
        }

        if (page != null) {
            contacts.setPageToken(page)
        }

        return contacts.execute()
    }

    override suspend fun fetchAll(watermark: String?, onDone: (String?) -> Unit, scope: CoroutineScope) = scope.produce {
        try {

            // TODO remove deleted calendars?

            logger.debug("Fetching contacts: ${account.id} ${watermark}")

            var newWatermark: String? = null
            var contacts = fetchPaged(null, watermark)
            var q = 0
            while (contacts != null && contacts.isNotEmpty()) {
                contacts.connections?.forEach {
                    send(convert(it))
                    q += 1
                }
                newWatermark = contacts.nextSyncToken

                // next page
                if (contacts.nextPageToken != null) {
                    contacts = fetchPaged(contacts.nextPageToken, watermark)
                } else {
                    contacts = null
                }
            }

            logger.debug("Fetched ${q} contacts for ${account.id} / ${newWatermark}")
            onDone(newWatermark)
        } catch (e: GoogleJsonResponseException) {
            if (e.statusCode == 401) {
                throw UnauthorizedException(account.username, e)
            }
        }
    }

    private fun convert(data: Person): Result {
//        logger.debug("DATA: ${data}")
        return Results.contact(
                originalId = data.resourceName,
                name = data.names?.firstOrNull()?.displayName ?: "",
                emails = data.emailAddresses?.map { it.value ?: "" } ?: emptyList(),
                companies = data.organizations?.map { it.name ?: "" } ?: emptyList(),
                jobs = data.organizations?.map { listOfNotNull(it.title, it.name).joinToString(", ").trim() } ?: emptyList(),
                phones = data.phoneNumbers?.map { it.canonicalForm ?: "" } ?: emptyList(),
                addresses = data.addresses?.map { it.formattedValue ?: "" } ?: emptyList(),
                birthday = data.birthdays?.map { it.date.toPrettyString() }?.firstOrNull(),
                thumbnail = data.photos?.firstOrNull()?.url,
                accountId = account.id,
                accountType = account.type,
                permalink = null, // TODO
                timestamp = 0,
                involvesMe = true
        )
    }

    private val client = GoogleApis.googleContactsClient(account)
}
