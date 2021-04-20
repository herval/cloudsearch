package cloudsearch.cache

import cloudsearch.content.Result
import cloudsearch.storage.AccountConfig
import cloudsearch.storage.FavoritesStorage
import kotlinx.coroutines.CoroutineScope
import org.slf4j.LoggerFactory

// expire any kind of expired cached content for the given account
class ExpireCached(
        val account: AccountConfig,
        val storage: ResultsCache,
        val favorites: FavoritesStorage
) : Refreshable {
    val log = LoggerFactory.getLogger(javaClass.name)

    override suspend fun refreshAll(ids: List<String>, scope: CoroutineScope): Map<String, Result?> {
        if (ids.isEmpty()) {
            return emptyMap()
        }

        log.debug("Expiring content: ${ids}")

        storage.deleteAllById(
                ids.filterNot {
                    favorites.isFavorited(it) // don't remove favorited
                }
        )

        return ids.map { it to null }.toMap()
    }
}
