package cloudsearch.filter

import cloudsearch.content.Result
import cloudsearch.search.Query
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

/**
 * Created by herval on 12/9/17.
 */
class FilterNotInRange(val query: Query) : Filter {
    val before = query.before?.millis ?: Long.MAX_VALUE
    val after = query.after?.millis ?: -1L
    val log = LoggerFactory.getLogger(javaClass.name)

    override fun keep(r: Result): Boolean {

        return if (inRange(after, before, r)) {
            true
        } else {
            log.debug("Discarded result out of range ${r.id} ${r.javaClass.simpleName} ${DateTime(r.timestamp)} for ${query}")
            false
        }
    }

    private fun inRange(before: Long, after: Long, result: Result): Boolean {
        return result.timestamp in before..after
    }
}
