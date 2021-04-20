package cloudsearch.clients.http

import kotlinx.coroutines.Deferred

interface Cancellable<T> {
    val get: Deferred<T?>
    fun cancel(): Unit
}
