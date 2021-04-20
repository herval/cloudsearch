package cloudsearch.server

import cloudsearch.account.AccountType
import cloudsearch.auth.AuthBuilder
import cloudsearch.clients.google.GoogleAuthRefresh
import cloudsearch.config.Config
import cloudsearch.config.ServiceCredentials
import cloudsearch.index.IndexerBuilder
import cloudsearch.index.Indexers
import cloudsearch.search.MultiSearch
import cloudsearch.search.OnlyMostRecentSearch
import cloudsearch.search.SearchBuilder
import cloudsearch.server.api.ServerConfig
import cloudsearch.server.repository.AccountConfigRepository
import cloudsearch.server.repository.FavoritesRepository
import cloudsearch.server.repository.MetadataRepository
import cloudsearch.server.repository.SearchableRepository
import cloudsearch.server.storage.DatabaseConfig
import cloudsearch.server.table.Results
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.slf4j.LoggerFactory

/**
 * Cloud-based server implementation
 */
object Main {
    private val logger = LoggerFactory.getLogger(javaClass.name)

    @JvmStatic
    fun main(args: Array<String>) {
        val conf = Config(".env")

        val storageConfig = DatabaseConfig.fromConfig(conf)
        val serverConfig = ServerConfig.fromConfig(conf)
        logger.info("*** Pointing storage to ${storageConfig.driver} ***")

        val accountConfig = AccountConfigRepository()
//        val storage = StorageBuilder(storageConfig)
        val favorites = FavoritesRepository()
        val metadata = MetadataRepository()
        val cache = SearchableRepository(Results())
        val credentials = ServiceCredentials.fromConfig(conf)
        val googleRefresh = GoogleAuthRefresh(credentials.get(AccountType.Google))
        val clients = SearchBuilder(cache, googleRefresh, favorites, credentials)
        val auth = AuthBuilder(credentials, serverConfig, clients)
        val search = OnlyMostRecentSearch(MultiSearch(), clients, accountConfig)
        val indexers = Indexers(
                CoroutineScope(Dispatchers.IO),
                IndexerBuilder(
                        CoroutineScope(Dispatchers.IO),
                        accountConfig,
                        metadata,
                        cache,
                        auth,
                        googleRefresh
                ),
                accountConfig
        )
//
//        val api = Server(
//                serverConfig,
//                storage.accountStorage,
//                storage.favoritesMetadata,
//                auth,
//                search,
//                indexers
//        )
//
//        runBlocking {
//            api.start()
//        }
    }

}