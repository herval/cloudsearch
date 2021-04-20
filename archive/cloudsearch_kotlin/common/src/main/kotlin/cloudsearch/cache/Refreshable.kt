package cloudsearch.cache

import cloudsearch.content.Result
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

interface Refreshable {
    suspend fun refreshAll(ids: List<String>, scope: CoroutineScope): Map<String, Result?>
}