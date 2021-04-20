package cloudsearch.index

import cloudsearch.cache.ResultsCache
import cloudsearch.content.Result
import cloudsearch.search.ContentHolder
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Seconds
import org.slf4j.LoggerFactory

class CachedFetch(
        val cache: ResultsCache,
        val fetcher: IndividualFetchable
) : IndividualFetchable, ContentHolder {
    override val contentKinds = fetcher.contentKinds
    private val logger = LoggerFactory.getLogger(javaClass.name)
    private val cacheTtl = Duration.standardMinutes(10).toStandardSeconds() // only return cached items newer than this

    suspend override fun fetch(contentId: String): Result? {
        val now = DateTime.now()
        val cached = cache.get(contentId)
        return if (cached != null) {
            logger.debug("Found on cache: ${cached.id}")
            return if (Seconds.secondsBetween(now, DateTime(cached.cachedAt)).isGreaterThan(cacheTtl)) {
                // expired!
                logger.debug("Expired: ${cached.id}")
                fetchRemote(contentId)
            } else {
                cached
            }
        } else {
            fetchRemote(contentId)
        }
    }

    private suspend fun fetchRemote(id: String): Result? {
        val remote = fetcher.fetch(id)
        if (remote == null) {
            logger.debug("Content no longer exists: ${id}")
            cache.deleteById(id)
        } else {
            cache.saveOrUpdate(remote)
        }
        return remote
    }
}