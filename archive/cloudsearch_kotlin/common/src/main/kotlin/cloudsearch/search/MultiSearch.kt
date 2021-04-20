package cloudsearch.search

import cloudsearch.content.Result
import cloudsearch.filter.Filter
import cloudsearch.storage.AccountConfig
import cloudsearch.storage.AccountConfigStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.LoggerFactory

/**
 * Created by hfreire on 1/8/17.
 */
class MultiSearch {
    private val logger = LoggerFactory.getLogger(javaClass.name)

    suspend fun search(
            accounts: List<AccountConfig>,
            searchables: List<Searchable>,
            filters: List<Filter>,
            query: Query,
            onResult: (Result) -> Unit,
            onFinish: (AccountConfig, Int) -> Unit,
            scope: CoroutineScope
    ) {
        val jobs = searchables.map { searchable ->
            scope.async {
                var q = 0
                try {
                    if (isActive) {
//                        logger.info("searching on Thread > " + Thread.currentThread().name)
                        searchable.search(
                                query.copy(
                                        accounts = accounts // scope the query to a single account at a time
                                ),
                                scope // TODO avoid cancelation from propagating to other searchables?
                        ).consumeEach {
                            //                logger.debug("consuming")
                            if (isActive) {
                                val stop = filters.firstOrNull { f -> !f.keep(it) || f.halt() }
                                if (stop == null) { // all filters passed
                                    q += 1
                                    onResult(it)
                                } else {
                                    if (stop.halt()) {
                                        coroutineContext.cancel()
//                                        logger.debug("${stop} asked to stop consuming, closing...")
                                        return@consumeEach
                                    }
                                }
                            } else {
                                logger.debug("not active, closing")
                                return@consumeEach
                            }
                        }
                    } else {
                        logger.debug("not active, skipping search")
                    }
                } catch (e: CancellationException) {
                    logger.debug("Job cancelled")
                } catch (e: ClosedSendChannelException) {
                    logger.debug("Channel closed")
                }
                accounts.forEach {
                    onFinish(it, q) // TODO this isn't working as expected
                }
            }
        }

        jobs.forEach { it.await() }
    }
}

// constrain a list of jobs and make sure we only ever yield the last requested term
class OnlyMostRecentSearch(
        val search: MultiSearch,
        val builder: SearchBuilder,
        val accountStorage: AccountConfigStorage
) {
    private var latestSearchTerm: Query? = null
    private var currentSearchId: String = ""
    private val inFlight = mutableListOf<Pair<String, Job>>()
    private val logger = LoggerFactory.getLogger(javaClass.name)
    //    private val pool = newFixedThreadPoolContext(2, "search")
//    private val cacheSearch = newSingleThreadContext("cache_search")

    fun search(
            scope: CoroutineScope,
            query: Query,
            searchId: String,
            onResult: (Result) -> Unit,
            onFinish: (AccountConfig, Int) -> Unit,
            onFinishAll: (Int) -> Unit
    ) {
        latestSearchTerm = query
        try {
            // return nothing
            if (query.fullText.isEmpty()) {
                logger.debug("Empty search, doing nothing")
            } else {
                // new search begins
//                val ctx = if (query.searchMode == SearchMode.Cache) {
//                    cacheSearch
//                } else {
//                    newSingleThreadContext("search_" + searchId)
//                }
                val currentSearch = scope.launch {
                    var q = 0
                    val term = latestSearchTerm
                    logger.debug("Searching: ${query}")
                    val accs = accountStorage.active(query.accountTypes)
                    search.search(
                            accs,
                            builder.all(
                                    accs,
                                    query.searchMode,
                                    query.contentTypes
                            ),
                            onFinish = onFinish,
                            query = query,
                            filters = builder.filters(query),
                            onResult = {
                                if (term == latestSearchTerm) { // still interested?
                                    q += 1
//                                    logger.debug("Sending: ${it.id}")
                                    onResult(it)
//                                } else {
//                                    logger.debug("Discarding result: ${it.id} ${it.type} ${it.title}")
                                }
                            },
                            scope = scope
                    )
                    logger.debug("Total results: ${q}")
                    onFinishAll(q)
                }

                synchronized(currentSearchId) {
                    currentSearchId = searchId
                }
                synchronized(inFlight) {
                    inFlight.add(currentSearchId to currentSearch)
                }
                cancelPreviousSearches(searchId)
            }
        } catch (e: RuntimeException) {
            logger.error("failed: " + e.message, e)
        }
    }

    private fun cancelPreviousSearches(currentId: String?) {
        logger.debug("in flight: ${inFlight.size}")
        synchronized(inFlight) {

            inFlight.filter { it.first != currentId }.forEach { expiredJob ->
                if (expiredJob.second.isActive) {
                    try {
                        logger.debug("Flagging for cancel")
                        expiredJob.second.cancel()
                    } catch (e1: IllegalStateException) {
                        logger.debug("[E1] Cancelled?!")
                    } catch (ee: CancellationException) {
                        logger.debug("[E2] Cancelled?!")
                    }
                }
            }

            inFlight.removeAll { !it.second.isActive }
        }
    }

    fun stop() {
        cancelPreviousSearches(null)
    }
}
