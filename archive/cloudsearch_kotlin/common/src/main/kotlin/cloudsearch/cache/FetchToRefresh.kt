package cloudsearch.cache

import cloudsearch.content.Result
import cloudsearch.index.Fetchable
import cloudsearch.storage.AccountConfig
import kotlinx.coroutines.CoroutineScope

class FetchToRefresh(val account: AccountConfig, val cache: ResultsCache, val fetcher: Fetchable) : Refreshable {

    override suspend fun refreshAll(ids: List<String>, scope: CoroutineScope): Map<String, Result?> {
        val res = mutableMapOf<String, Result?>()
        val found = fetcher.fetchAll(ids, scope)
        found.map { (id, updated) ->
            if (updated == null) { // removed downstream
                cache.deleteById(id)
                res.put(id, null)
            } else {
                // update & send found
                cache.saveOrUpdate(updated)
                res.put(id, updated)
            }
        }

        return res
    }
}