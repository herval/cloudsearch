package cloudsearch.embedded.cache

import cloudsearch.cache.ResultsCache
import cloudsearch.content.Result
import cloudsearch.embedded.storage.KeyValueStore
import cloudsearch.embedded.storage.StorageConfig
import cloudsearch.search.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.isActive
import org.slf4j.LoggerFactory

class SearchableCache(
        storage: StorageConfig,
        val searchQuery: SearchQuery,
        tolerateMissedWrites: Boolean = true
) : KeyValueStore<Result>(
        Result::class,
        "results",
        storage,
        tolerateMissedWrites,
        cleanupOnFail = true
), ResultsCache {
    private val log = LoggerFactory.getLogger(javaClass.name)

    override suspend fun search(query: Query, scope: CoroutineScope) = scope.produce {
        log.debug("Searching cache: ${query.fullText}")
        val q = searchQuery.query(query)
        if (q == null) {
            log.debug("No query for [${query}]")
        } else {
            val r = data.retrieve(
                    q.query,
                    q.options
            )

            // offset & limit
            r.take(query.maxResults * 10) // getting more results so we can discard irrelevant ones next
                    .sortedBy { -it.relevance() }
                    .drop(query.offset)
                    .take(query.maxResults)
                    .forEach { res ->
                        if (isActive) {
                            send(res as Result)
                        } else {
                            return@forEach
                        }
                    }
        }
    }

}
