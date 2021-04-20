package cloudsearch.embedded.api

import cloudsearch.account.AccountType
import cloudsearch.api.ServerEndpoints
import cloudsearch.auth.AuthBuilder
import cloudsearch.auth.OauthRedirectConfig
import cloudsearch.config.Config
import cloudsearch.embedded.storage.AccountConfigDiskStorage
import cloudsearch.embedded.storage.FavoritesService
import cloudsearch.index.Indexers
import cloudsearch.search.OnlyMostRecentSearch
import cloudsearch.util.pid
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import spark.Request
import spark.Response
import spark.Spark.get
import spark.template.handlebars.HandlebarsTemplateEngine
import kotlin.system.exitProcess

data class ServerConfig(val port: Int) : OauthRedirectConfig {
    companion object {
        fun fromConfig(c: Config): ServerConfig {
            return ServerConfig(
                    c.get("PORT")?.toInt() ?: 65432
            )
        }
    }

    override fun redirectUrl(type: AccountType): String {
        return "http://localhost:${port}/accounts/oauth/callback/${type.name}"
    }

}

/**
 * Created by herval on 12/14/17.
 */
class Server(
        val conf: ServerConfig,
        val accountStorage: AccountConfigDiskStorage,
        val favoritesStorage: FavoritesService,
        val auth: AuthBuilder,
        val search: OnlyMostRecentSearch,
        val indexers: Indexers
) : ServerEndpoints() {

    private val logger = LoggerFactory.getLogger(javaClass.name)

    override fun search(): OnlyMostRecentSearch = search

    override fun auth(): AuthBuilder = auth

    override fun port(): Int = conf.port

    override fun ipAddress(): String = "0.0.0.0"

    override fun accountStorage(): AccountConfigDiskStorage = accountStorage

    override fun favoritesStorage() = favoritesStorage

    override fun htmlRenderer() = HandlebarsTemplateEngine()

    override fun beforeAll(req: Request, res: Response) {
    }


    override fun extraRoutes() {
        GlobalScope.launch {
            indexers.start()
        }

        // TODO limit remote
//        threadPool(10)

        // these endpoints are embedded-only
        get("/kill", { _, _ ->
            logger.info("Bye!")
            exitProcess(0)
        })

        get("/health", { _, res ->
            json(
                    res,
                    mapOf(
                            "pid" to pid().toString()
                    )
            )
        })
    }

}
