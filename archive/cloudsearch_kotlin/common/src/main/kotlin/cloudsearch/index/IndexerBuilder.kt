package cloudsearch.index

import cloudsearch.account.AccountType
import cloudsearch.auth.AuthBuilder
import cloudsearch.cache.ResultsCache
import cloudsearch.clients.google.GoogleAuthRefresh
import cloudsearch.clients.google.GoogleCalendarFetch
import cloudsearch.clients.google.GoogleContactsFetch
import cloudsearch.storage.AccountConfig
import cloudsearch.storage.AccountConfigStorage
import cloudsearch.storage.Storage
import kotlinx.coroutines.CoroutineScope
import org.slf4j.LoggerFactory

class IndexerBuilder(
        val scope: CoroutineScope,
        val accountStorage: AccountConfigStorage,
        val indexingMetadata: Storage<IndexingMetadata>,
        val cache: ResultsCache,
        val auth: AuthBuilder,
        val googleAuthRefresh: GoogleAuthRefresh
) {
    val log = LoggerFactory.getLogger(javaClass.name)

    private fun <A : AccountConfig> indexer(
            account: A,
            cache: ResultsCache,
            indexer: BulkIndexable
    ): Indexer {
        return Indexer(
                scope,
                account,
                accountStorage,
                indexingMetadata,
                cache,
                indexer
        )
    }

    private fun contacts(account: AccountConfig) = indexer(
            account,
            cache,
            GoogleContactsFetch(account)
    )


    private fun calendars(acc: AccountConfig) = indexer(
            acc,
            cache,
            GoogleCalendarFetch(acc)
    )

    suspend fun all(accounts: List<AccountConfig>): List<Indexer> {
        return accounts.map {
            when (it.type) {
                AccountType.Google -> {
                    val acc = googleAuthRefresh.refreshToken(it) // since these objects are short-lived, we only need to refresh auth here/once
                    listOf(
                            contacts(acc),
                            calendars(acc)
                    )
                }
                else -> emptyList()
            }
        }.flatten()
    }
}