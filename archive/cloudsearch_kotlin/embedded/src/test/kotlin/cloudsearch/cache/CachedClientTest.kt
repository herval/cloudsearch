package cloudsearch.cache

import cloudsearch.account.Account
import cloudsearch.config.Config
import cloudsearch.content.ContentType
import cloudsearch.content.Result
import cloudsearch.content.Results
import cloudsearch.embedded.cache.BasicQuery
import cloudsearch.embedded.cache.SearchableCache
import cloudsearch.embedded.storage.FavoritesService
import cloudsearch.embedded.storage.StorageConfig
import cloudsearch.search.ContentSearchable
import cloudsearch.search.Query
import cloudsearch.search.SearchAndCache
import junit.framework.TestCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import kotlin.coroutines.CoroutineContext

class CachedClientTest : TestCase() {

    val conf = Config(".env.test")
    val storage = StorageConfig.fromConfig(conf)
    val cache = SearchableCache(storage, BasicQuery())

    val favorites = FavoritesService(cache)

    val account = Account.dropbox("123", "123", "email")

    val cached = SearchAndCache(
            account,
            cache,
            mockSearch(emptyList<Result>()),
            ExpireCached(account, cache, favorites),
            InMemoryStorage()
    )

    private fun mockSearch(results: List<Result>): ContentSearchable {
        return object : ContentSearchable {
            override val contentKinds = listOf(ContentType.File)

            override suspend fun search(query: Query, scope: CoroutineScope) = scope.produce<Result> {
                results.forEach { send(it) }
            }
        }
    }

    fun testCachedSearch() {
        runBlocking {
            cache.clear()
            assertTrue(
                    cache.saveOrUpdate(
                            Results.file(
                                    originalId = "foo-id",
                                    accountId = "foo-source",
                                    title = "foo-title",
                                    body = "foo-desc",
                                    accountType = account.type,
                                    path = "foopath",
                                    permalink = "foofoo",
                                    timestamp = DateTime.now().millis,
                                    involvesMe = true,
                                    sizeBytes = 1,
                                    thumbnail = "",
                                    labels = null
                            )
                    )
            )
            val found = cached.search(
                    Query.parsed("foo-title")!!,
                    GlobalScope
            )

            assertNotNull(found.receiveOrNull())
        }
    }
}