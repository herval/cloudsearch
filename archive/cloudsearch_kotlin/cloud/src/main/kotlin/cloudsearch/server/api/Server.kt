//package cloudsearch.server.api
//
//import cloudsearch.api.ServerEndpoints
//import cloudsearch.auth.AuthBuilder
//import cloudsearch.index.Indexers
//import cloudsearch.search.OnlyMostRecentSearch
//import cloudsearch.server.auth.Validator
//import cloudsearch.storage.AccountConfigStorage
//import org.slf4j.LoggerFactory
//import spark.Request
//import spark.Response
//import spark.kotlin.halt
//import spark.template.handlebars.HandlebarsTemplateEngine
//
//class Server(
//        val conf: ServerConfig,
//        val accountStorage: AccountConfigStorage,
//        val favoritesStorage: FavoritesStorage,
//        val auth: AuthBuilder,
//        val search: OnlyMostRecentSearch,
//        val indexers: Indexers,
//        val validator: Validator
//) : ServerEndpoints() {
//    private val logger = LoggerFactory.getLogger(javaClass.name)
//
//    override fun beforeAll(req: Request, res: Response) {
//        // validate auth token
//        if(!validator.isAuthorized(req.headers("Authentication"))) {
//            halt(403)
//        }
//    }
//
//    override fun extraRoutes() {
//        indexers.start()
//    }
//
//    override fun search(): OnlyMostRecentSearch = search
//
//    override fun auth(): AuthBuilder  = auth
//
//    override fun port(): Int = conf.port
//
//    override fun ipAddress(): String  = "0.0.0.0"
//
//    override fun accountStorage(): AccountConfigStorage  = accountStorage
//
//    override fun favoritesStorage(): FavoritesStorage = favoritesStorage
//
//    override fun htmlRenderer() = HandlebarsTemplateEngine()
//}