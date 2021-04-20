package cloudsearch.filter

import cloudsearch.content.Result
import cloudsearch.search.Query
import org.slf4j.LoggerFactory

/**
 * Created by herval on 12/9/17.
 */
class Dedup(val q: Query) : Filter {
    val log = LoggerFactory.getLogger(javaClass.name)
    val alreadyPosted = mutableSetOf<String>()

    override fun keep(r: Result): Boolean {
        return if (!alreadyPosted.contains(r.id)) {
            alreadyPosted.add(r.id)
            true
        } else {
            log.debug("Discarded dup result ${r.id} for ${q}")
            false
        }
    }
}
