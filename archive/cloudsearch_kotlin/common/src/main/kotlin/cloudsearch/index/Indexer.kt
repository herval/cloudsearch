package cloudsearch.index

import cloudsearch.auth.UnauthorizedException
import cloudsearch.cache.ResultsCache
import cloudsearch.storage.AccountConfig
import cloudsearch.storage.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.consumeEach
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

class Indexer(
        val scope: CoroutineScope,
        val account: AccountConfig,
        val accountStorage: Storage<AccountConfig>,
        val metadata: Storage<IndexingMetadata>,
        val cache: ResultsCache,
        val underlying: BulkIndexable
) {
    private val log = LoggerFactory.getLogger(javaClass.name)

    suspend fun update() {
        val meta = metadata.get(account.id) ?: newMetadata()
//        log.debug("Indexing meta: ${meta}")

        // index data all the time
        try {
            underlying.fetchAll(
                    watermark = meta.watermark,
                    onDone = { newWatermark ->
                        log.debug("Done syncing ${account.id} - new watermark: ${newWatermark}")
                        metadata.saveOrUpdate(
                                meta.copy(
                                        watermark = newWatermark,
                                        lastIndexedAt = DateTime.now()
                                )
                        )
//                    log.debug("New meta: ${metadata.get(account.id)}")
                    },
                    scope = scope
            ).consumeEach {
                cache.saveOrUpdate(it)
            }
        } catch (e: UnauthorizedException) {
            log.info("Account is no longer authorized, stoppping indexing: ${account.id}")
            accountStorage.saveOrUpdate(
                    account.copy(active = false) // deactivate borked accounts
            )
        }

        // TODO expire old - for each contact with lastSeen > TTL, get in batch and delete not found/update found
    }

    private fun newMetadata(): IndexingMetadata {
        val m = IndexingMetadata(
                account.id, null, null
        )
        metadata.saveOrUpdate(m)
        return m
    }
}