package cloudsearch

import cloudsearch.account.AccountType
import cloudsearch.cache.InMemoryStorage
import cloudsearch.cache.ResultsCache
import cloudsearch.clients.google.*
import cloudsearch.config.Config
import cloudsearch.config.ServiceCredentials
import cloudsearch.content.Result
import cloudsearch.search.Query
import junit.framework.TestCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

class GoogleClientsTest : TestCase() {
    val conf = Config(".env.test")
    val credentials = ServiceCredentials.fromConfig(conf)

    val calendarStorage: ResultsCache = object : ResultsCache, InMemoryStorage<Result>() {
        suspend override fun search(query: Query, scope: CoroutineScope) = scope.produce {
            getAll().forEach {
                send(it)
            }
        }
    }

    val googleAuthRefresh = GoogleAuthRefresh(credentials.get(AccountType.Google))

//    val googleAuth = GoogleAuth(
//            credentials.get(GoogleAccount::class),
//            { GoogleContactsFetch(it) },
//            ServerConfig.fromConfig(conf)
//    )

    fun calendarFetch() = GoogleCalendarFetch(Accounts.googleAccount)
    fun events() = GoogleCalendarEventSearch(Accounts.googleAccount, calendarStorage)
    fun contacts() = GoogleContactsFetch(Accounts.googleAccount)
    fun gmailFetch() = GmailFetch(Accounts.googleAccount, credentials.get(AccountType.Google))
    fun gmail() = GmailSearch(Accounts.googleAccount, gmailFetch(), credentials.get(AccountType.Google))

    override fun setUp() = runBlocking {
        println("Refreshing token...")
        Accounts.googleAccount = googleAuthRefresh.refreshToken(Accounts.googleAccount)
        println("Token: ${Accounts.googleAccount.credentials}")
    }

    fun testSearchEvents() = runBlocking {
        println("Fetching calendars...")
        calendarFetch().fetchAll(null, {}, this).consumeEach {
            println("Calendar: ${it}")
            calendarStorage.saveOrUpdate(it)
        }

        val r = events().search(
                Query.parsed("foo")!!,
                this
        )

        assertNotNull(r.receiveOrNull())
    }

    fun testGmail() = runBlocking {
        val r = gmail().search(
                Query.parsed("herval")!!,
                this
        )
        assertNotNull(r.receiveOrNull())
    }

    fun testFetchContacts() = runBlocking {
        val me = contacts().me()
        assertNotNull(me?.emailAddresses)

        val contacts = contacts().fetchAll(null, {}, this)
        val c1 = contacts.receive()
        assertNotNull(c1.title)
        assert((c1.details["emails"]!! as List<String>).isNotEmpty())
        assert((c1.details["phones"]!! as List<String>).isNotEmpty())
        assertNotNull(c1.thumbnail)
    }
}