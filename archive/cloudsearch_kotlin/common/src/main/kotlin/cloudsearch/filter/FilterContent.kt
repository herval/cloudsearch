package cloudsearch.filter

import cloudsearch.content.ContentType
import cloudsearch.content.Files
import cloudsearch.content.Result
import cloudsearch.search.Query
import org.slf4j.LoggerFactory

// highest level content filter to avoid returning garbage from downstreams that won't filter
class FilterContent(val query: Query) : Filter {
    val log = LoggerFactory.getLogger(javaClass.name)
    val contentToKeep = query.contentTypes

    override fun keep(r: Result): Boolean {
        return if (isAcceptable(r.type)) {
            true
        } else {
            log.debug("Filtered out result ${r.id} / ${r.type} for ${query.fullText}")
            false
        }
    }

    fun isAcceptable(c: ContentType): Boolean {
        // generalize some types
        return contentToKeep == null ||
                ((contentToKeep.contains(ContentType.File) && Files.isFileType(c)) || contentToKeep.contains(c))
    }
}