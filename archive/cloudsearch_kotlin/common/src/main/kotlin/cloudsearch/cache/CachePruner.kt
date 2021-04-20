package cloudsearch.cache

//    private val maxCacheSize = 10 // maximum cache size

//    val pruningJob = Thread {
//        while (true) {
//            // TODO cache pruning - keep max elements only (delete least recently updated)
//            if(cache.count() > maxCacheSize) {
//                logger.debug("Cache is too big! Pruning...")
//                // TODO
//            }
//            sleep(30000)
//        }
//    }.start()