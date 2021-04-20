//package cloudsearch.server.cache
//
//import cloudsearch.cache.CacheLocator
//import cloudsearch.cache.ResultsCache
//import cloudsearch.content.Result
//import cloudsearch.storage.AccountConfig
//import org.slf4j.LoggerFactory
//import kotlin.reflect.KClass
//
//
//// a façade for mapping content types and accounts to different cache files
//class CacheBuilder() : CacheLocator {
//    val logger = LoggerFactory.getLogger(javaClass.name)
//
//    // result caches are per account per content type
//    private val resultCaches = mutableMapOf<String, ResultsCache<*>>()
//
//    override fun <T : Result, A : AccountConfig> cacheFor(account: A, contentType: KClass<T>): ResultsCache<T> {
//        val k = key(account, contentType)
//        return resultCaches.get(k).let {
//            if (it == null) {
//                logger.debug("Building content cache for ${account.id} / ${contentType}")
//                val q = queryFor(contentType) as SearchQuery<T>
//                val c = SearchableCache(contentType, account, config, q)
//                resultCaches.put(k, c)
//                c
//            } else {
////                logger.debug("Reusing cache: ${it}")
//                it as SearchableCache<T, A>
//            }
//        }
//    }
//
//    private fun <T : Result> key(account: AccountConfig, contentType: KClass<T>): String {
//        return "${account.id}_${contentType.simpleName}"
//    }
//
//    fun <T : Result> queryFor(k: KClass<T>): SearchQuery<*> {
////        return when(k.objectInstance) {
//////            is Email -> GmailQuery()
//////            is GoogleDriveResult -> GoogleDriveQuery()
//////            is DropboxResult -> DropboxQuery()
////            else -> {
////                logger.debug("No special query engine configured for ${k}")
//        return BasicQuery(k)
////            }
////        }
//    }
//}