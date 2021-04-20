package cloudsearch.filter

import cloudsearch.content.Result
import cloudsearch.search.Query
import org.slf4j.LoggerFactory

/**
 * Created by herval on 12/9/17.
 */
class LimitResults(val query: Query) : Filter {
    val log = LoggerFactory.getLogger(javaClass.name)
    var q = 0

    override fun keep(r: Result): Boolean {
        return if (q > query.maxResults) {
            log.debug("Discarded excess results for ${query}")
            //close() // TODO close
            false

        } else {
            q += 1
            true
        }
    }

    override fun halt(): Boolean {
        return q > query.maxResults
    }
}
