package cloudsearch.api

import cloudsearch.auth.AuthBuilder
import cloudsearch.clients.http.OfflineException
import cloudsearch.content.Result
import cloudsearch.search.OnlyMostRecentSearch
import cloudsearch.search.Query
import cloudsearch.storage.AccountConfigStorage
import cloudsearch.storage.FavoritesStorage
import cloudsearch.util.Parsers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import spark.*
import spark.Spark.*
import java.net.UnknownHostException

abstract class ServerEndpoints {
    private val parser = Parsers.camelCased
    private val logger = LoggerFactory.getLogger(javaClass.name)
    abstract fun htmlRenderer(): TemplateEngine
    abstract fun search(): OnlyMostRecentSearch
    abstract fun auth(): AuthBuilder
    abstract fun accountStorage(): AccountConfigStorage
    abstract fun favoritesStorage(): FavoritesStorage
    abstract fun extraRoutes()
    abstract fun port(): Int
    open fun ipAddress(): String = "127.0.0.1"

    fun start() {
        Spark.port(port())
        Spark.ipAddress(ipAddress())

        // socket for search only
        webSocket("/socket/search", SearchSocket(parser, search()))

        before("*", { req, res ->
            beforeAll(req, res)
        })

        extraRoutes()

//        get("/contents/types", { req, res ->
//            json(
//                    res,
//                    Results.searchable
//            )
//        })

        get("/accounts/oauth/callback/:service", { req, res ->
            handleCode(req)
        }, htmlRenderer())

        get("/contents/favorite/:content_id", { req, res ->
            favoriteToggle(req, res)
        })

        get("/contents/thumbnail/:account_id/:content_id", { req, res ->
            //            favoriteToggle(req, res)
        })

        get("/contents/favorites", { _, res ->
            favorites(res)
        })

        get("/accounts/auth_url", { req, res ->
            authUrl(req, res)
        })

        get("/accounts/create", { req, res ->
            saveAccountWithToken(req, res)
        })

        get("/accounts/basic_auth/create", { req, res ->
            saveBasicAuthAccount(req, res)
        })

        get("/accounts/remove/:id", { req, res ->
            deleteAccount(req, res)
        })

        get("/accounts/list", { _, res ->
            listAccounts(res)
        })

        get("/search", { req, res ->
            searchResults(req, res)
        })

        init()
    }

    abstract fun beforeAll(req: Request, res: Response)

    private fun handleCode(req: Request): ModelAndView {
        return runBlocking {
            val service = req.params("service")
            val code = req.queryParams("code")
            val account = auth().accountFor(service, code)
            accountStorage().saveOrUpdate(account)

            ModelAndView(emptyMap<String, String>(), "close_page.hbs")
        }
    }

    private fun searchResults(req: Request, res: Response): String {
        return runBlocking {
            val results = mutableListOf<Result>()
            val query = Query.parsed(
                    req.queryParams("query")
            )
            if (query == null) {
                json(
                        res,
                        mapOf(
                                "results" to emptyList<Map<String, Any>>()
                        )
                )
            } else {
                var searching = true

                var err: String? = null
                try {
                    search().search(
                            query = query,
                            onFinish = { _, _ -> }, // not useful in this mode
                            onFinishAll = {
                                searching = false
                            },
                            searchId = "POST",
                            onResult = {
                                results.add(it)
                            },
                            scope = CoroutineScope(Dispatchers.IO)
                    )
                } catch (e: OfflineException) {
                    err = "offline_mode"
                }

                while (searching) {
                    delay(100)
                }

                json(
                        res,
                        mapOf(
                                "results" to results.map { it.toJson(query) }
                        ).plus(
                                if (err != null) {
                                    mapOf("error" to err)
                                } else {
                                    emptyMap()
                                }
                        )
                )
            }
        }
    }

    private fun listAccounts(res: Response): String {
        val accts = accountStorage().all().map { it.toJson() }
        return json(
                res,
                accts
        )
    }

    private fun deleteAccount(req: Request, res: Response) = runBlocking {
        val removed = accountStorage().deleteById(req.params("id"))

        json(
                res,
                mapOf("deleted" to removed)
        )
    }

    // for accounts with user/pass
    private fun saveBasicAuthAccount(req: Request, res: Response) = runBlocking {
        val a = try {
            auth().accountForBasicAuth(
                    req.queryParams("service"),
                    req.queryParams("username"),
                    req.queryParams("password"),
                    req.queryParams("server"),
                    this
            )
        } catch (e: UnknownHostException) {
            logger.debug("Can't create account, offline: ${e}")
            null
        } catch (e: Exception) {
            logger.error("Can't create account: ${e}", e)
            null
        }

        val saved = a?.let {
            accountStorage().saveOrUpdate(it)
        } ?: false

        json(
                res,
                mapOf(
                        "saved" to saved
                )
        )
    }

    private fun saveAccountWithToken(req: Request, res: Response) = runBlocking {
        // TODO validate
        val saved = accountStorage().saveOrUpdate(
                auth().accountFor(req.queryParams("service"), req.queryParams("token"))
        )

        json(
                res,
                mapOf("saved" to saved)
        )
    }

    private fun favorites(res: Response): String = runBlocking {
        json(
                res,
                mapOf(
                        "favorited" to favoritesStorage()
                                .getAll(this)
                                .map {
                                    it.toJson(Query())
                                }
                )
        )
    }

    private fun favoriteToggle(req: Request, res: Response): String {
        return json(
                res,
                mapOf("favorited" to favoritesStorage().toggle(req.params("content_id")))
        )
    }

    private fun authUrl(req: Request, res: Response): String {
        val url = auth().authUrlFor(req.queryParams("service"))
        return json(
                res,
                mapOf("url" to url)
        )
    }


    fun json(res: Response, content: Any): String {
        res.type("application/json")
        return parser.toJson(
                content
        )
    }
}
