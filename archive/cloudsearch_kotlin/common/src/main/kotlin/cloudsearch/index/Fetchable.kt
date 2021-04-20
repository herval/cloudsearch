package cloudsearch.index

import cloudsearch.content.Result
import cloudsearch.search.ContentHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext

// fetch content for a given account by id
interface Fetchable {

    suspend fun fetchAll(ids: List<String>, scope: CoroutineScope): Map<String, Result?>
}


interface IndividualFetchable : Fetchable, ContentHolder {

    suspend fun fetch(contentId: String): Result?

    override suspend fun fetchAll(ids: List<String>, scope: CoroutineScope): Map<String, Result?> {

        // TODO how do we handle failed fetches?

        val fetched = ids.map {
            scope.async {
                it to fetch(it)
            }
        }

        return fetched.map {
            it.await()
        }.toMap()
    }
}