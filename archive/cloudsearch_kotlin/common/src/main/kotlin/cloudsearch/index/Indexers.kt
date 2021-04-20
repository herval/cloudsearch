package cloudsearch.index

import cloudsearch.clients.http.OfflineException
import cloudsearch.storage.AccountConfigStorage
import cloudsearch.util.Network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.time.Duration

class Indexers(
        val scope: CoroutineScope,
        val indices: IndexerBuilder,
        val accountStorage: AccountConfigStorage
) {
    private val working = mutableMapOf<String, Job>()
    private val log = LoggerFactory.getLogger(javaClass.name)
    var online = true

    fun start() = scope.launch {
        log.info("Starting indexer...")
        Network.addListener {
            online = it
        }

        while (true) {
            // TODO make these run in parallel (one indexer per type max)
            try {
                if (online) {
                    val indexers = indices.all(accountStorage.active())
                    indexers.forEach {
                        val job = working.get(it.account.id)
                        if (job != null && job.isCompleted) {
                            log.debug("Job done: ${it.account.id}")
                            working.remove(it.account.id)
                        }

                        if (job == null) {
                            log.debug("Job scheduled: ${it.account.id}")
                            working.put(it.account.id,
                                    scope.launch {
                                        it.update()
                                    }
                            )
                        }
                    }
                }
                delay(Duration.ofMinutes(1).toMillis())
            } catch (e: OfflineException) {
                log.debug("Offline, will retry later")
            } catch (e: Exception) {
                log.error("INDEXER BORKED: ${e.message}", e)
            }
        }
    }

}