package cloudsearch.server.repository

import cloudsearch.content.Result
import cloudsearch.storage.FavoritesStorage
import kotlinx.coroutines.CoroutineScope

class FavoritesRepository: FavoritesStorage {
    suspend override fun getAll(scope: CoroutineScope): List<Result> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toggle(contentId: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isFavorited(contentId: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}