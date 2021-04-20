package cloudsearch.search

import cloudsearch.cache.InMemoryStorage
import cloudsearch.cache.Refreshable
import cloudsearch.cache.ResultsCache
import cloudsearch.content.ContentType
import cloudsearch.content.Result
import cloudsearch.storage.AccountConfig
import cloudsearch.util.Network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.isActive
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Seconds
import org.slf4j.LoggerFactory

class SearchAndCache(
        val account: AccountConfig,
        val cache: ResultsCache,
        val liveSearch: ContentSearchable, // underying live search
        val refresher: Refreshable, // refresh or remove stale items
        val queriesCache: InMemoryStorage<DateTime>
) : ContentSearchable {
    override val contentKinds = liveSearch.contentKinds

    private val logger = LoggerFactory.getLogger(javaClass.name)

    private val cacheOnlyTtl = Duration.standardSeconds(15).toStandardSeconds() // don't hit API for subsequent hits on the same query
    private val cacheTtl = Duration.standardMinutes(5).toStandardSeconds() // return cached items up to this & revalidate them

    // this will get wiped out from time to time (eg when an auth refresh happens) - it's just an optimization to hit the api less
//    private val queriesCache = mutableMapOf<Query, DateTime>()

    override suspend fun search(query: Query, scope: CoroutineScope) = scope.produce<Result> {
        val now = DateTime.now()
        val searchUnderlying = shouldLiveSearch(query, now)

        if (Network.online && searchUnderlying && isActive) {
            val expiredContent = mutableMapOf<String, Result>()
            val foundContent = mutableSetOf<String>()

            // similarity search on cached results
            val cachedSearches = searchCache(query, now, expiredContent, scope)

            val live = liveSearch(searchUnderlying, query, foundContent, this, scope)

            // un-expire found content in background
            cachedSearches.await()
            live.await()
            if (isActive) {
                refreshExpired(expiredContent.keys.toSet(), foundContent, scope).await()
            }
        } else {
            logger.debug("Skipping Underlying search")
        }

        // cache results for query
        queriesCache.saveOrUpdate(query.fullText, DateTime.now())
    }

    private fun shouldLiveSearch(query: Query, now: DateTime): Boolean {
        // search liveSearch only if more than cacheOnlyTtl seconds have elapsed for the same query
        val searched = queriesCache.get(query.fullText)
        if (searched == null || Seconds.secondsBetween(searched, now).isGreaterThan(cacheOnlyTtl)) {
            return true
        }

        return false
    }

    // fetch from cache for expiration purposes *only*
    private suspend fun searchCache(
            query: Query,
            now: DateTime?,
            expiredContent: MutableMap<String, Result>,
            scope: CoroutineScope
    ): Deferred<Unit> {
        return scope.async {
            cache.search(
                    query.copy(
                            contentTypes = typesToSearch(query)
                    ),
                    scope
            ).consumeEach { r ->
                // if any is not cached, searchUnderlying = true
                // if >= cacheOnlyTtl, searchUnderlying = true
                val delta = Seconds.secondsBetween(now, DateTime(r.cachedAt))

                if (delta.isGreaterThan(cacheTtl)) {
                    // expired!
                    logger.debug("Expired: ${r.id}")
                    expiredContent.put(r.id, r)
                }
            }
        }
    }

    // search either all content kinds on this cached search or a subset of them
    private fun typesToSearch(query: Query): List<ContentType> {
        return query.contentTypes?.let {
            contentKinds.intersect(query.contentTypes).toList()
        } ?: contentKinds
    }

    private suspend fun liveSearch(
            searchUnderlying: Boolean,
            query: Query,
            foundContent: MutableSet<String>,
            channel: ProducerScope<Result>,
            scope: CoroutineScope
    ) = scope.async {
        if (liveSearch.active() && channel.isActive && searchUnderlying) {
            logger.debug("Searching ${query} downstream")

            liveSearch.search(query, scope).consumeEach {
                if (channel.isActive) {
//                    logger.debug("Found downstream: ${it.id}")

                    val res = it.copy(
                            involvesMe = query.involving.contains(InvolvementType.Me)
                    )
                    cache.saveOrUpdate(it)

                    foundContent.add(it.id)
                    channel.send(res)
                } else {
                    channel.close()
                    return@consumeEach
                }
            }
        }
    }

    private suspend fun refreshExpired(
            expiredContent: Set<String>,
            foundContent: Set<String>,
            scope: CoroutineScope
    ) = scope.async {
        // refresh all content that wasn't found on live search
        refresher.refreshAll(
                expiredContent.filterNot { foundContent.contains(it) }, scope
        )

    }

}