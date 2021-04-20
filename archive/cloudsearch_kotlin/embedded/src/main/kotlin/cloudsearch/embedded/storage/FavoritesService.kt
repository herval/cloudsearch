package cloudsearch.embedded.storage

import cloudsearch.content.Result
import cloudsearch.embedded.cache.SearchableCache
import cloudsearch.search.Query
import cloudsearch.search.ResultStatus
import cloudsearch.storage.FavoritesStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.fold
import kotlin.coroutines.CoroutineContext

// favorites do not get saved as a sepparate thing
class FavoritesService(
        val cache: SearchableCache
) : FavoritesStorage {

    override suspend fun getAll(scope: CoroutineScope): List<Result> {
        return cache.search(
                Query(
                        statuses = listOf(ResultStatus.Favorited)
                ),
                scope
        ).fold(emptyList<Result>()) { list, res ->
            list.plus(res)
        }
    }

    override fun isFavorited(contentId: String): Boolean {
        return cache.get(contentId)?.favorited ?: false
    }

    override fun toggle(contentId: String): Boolean {
        val content = cache.get(contentId)
        return if (content != null) {
            cache.saveOrUpdate(
                    content.copy(
                            favorited = !content.favorited
                    )
            )
            true
        } else {
            false
        }
    }
}