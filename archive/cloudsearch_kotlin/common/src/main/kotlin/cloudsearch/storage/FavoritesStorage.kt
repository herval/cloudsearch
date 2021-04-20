package cloudsearch.storage

import cloudsearch.content.Result
import kotlinx.coroutines.CoroutineScope

interface FavoritesStorage {
    fun toggle(contentId: String): Boolean
    fun isFavorited(contentId: String): Boolean
    suspend fun getAll(scope: CoroutineScope): List<Result>
}