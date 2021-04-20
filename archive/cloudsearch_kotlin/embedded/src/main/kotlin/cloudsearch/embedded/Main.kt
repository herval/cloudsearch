package cloudsearch.embedded

import cloudsearch.account.AccountType
import cloudsearch.auth.AuthBuilder
import cloudsearch.clients.google.GoogleAuthRefresh
import cloudsearch.config.Config
import cloudsearch.config.ServiceCredentials
import cloudsearch.embedded.api.Server
import cloudsearch.embedded.api.ServerConfig
import cloudsearch.embedded.cache.BasicQuery
import cloudsearch.embedded.cache.SearchableCache
import cloudsearch.embedded.storage.AccountConfigDiskStorage
import cloudsearch.embedded.storage.FavoritesService
import cloudsearch.embedded.storage.KeyValueStore
import cloudsearch.embedded.storage.StorageConfig
import cloudsearch.index.IndexerBuilder
import cloudsearch.index.Indexers
import cloudsearch.index.IndexingMetadata
import cloudsearch.search.MultiSearch
import cloudsearch.search.OnlyMostRecentSearch
import cloudsearch.search.SearchBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Created by hfreire on 1/8/17.
 */
object Main {
    private val logger = LoggerFactory.getLogger(javaClass.name)

    @JvmStatic
    fun main(args: Array<String>) {
        val conf = Config(".env")

        val storageConfig = StorageConfig.fromConfig(conf)
        val serverConfig = ServerConfig.fromConfig(conf)
        logger.info("*** Pointing storage to ${storageConfig.storagePath} ***")

        val cache = SearchableCache(storageConfig, BasicQuery())
        val favorites = FavoritesService(cache)
        val credentials = ServiceCredentials.fromConfig(conf)
        val googleRefresh = GoogleAuthRefresh(credentials.get(AccountType.Google))
        val clients = SearchBuilder(cache, googleRefresh, favorites, credentials)
        val auth = AuthBuilder(credentials, serverConfig, clients)
        val accountStorage = AccountConfigDiskStorage(storageConfig)
        val search = OnlyMostRecentSearch(MultiSearch(), clients, accountStorage)
        val indexingMetadata = KeyValueStore(IndexingMetadata::class, "indexingMetadata", storageConfig)
        val indexers = Indexers(
                CoroutineScope(Dispatchers.IO),
                IndexerBuilder(
                        CoroutineScope(Dispatchers.IO),
                        accountStorage,
                        indexingMetadata,
                        cache,
                        auth,
                        googleRefresh
                ),
                accountStorage
        )

        val api = Server(
                serverConfig,
                accountStorage,
                favorites,
                auth,
                search,
                indexers
        )

        runBlocking {
            api.start()
        }
    }

}