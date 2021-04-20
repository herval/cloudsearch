package cloudsearch.index

import cloudsearch.storage.Identifiable
import org.joda.time.DateTime

data class IndexingMetadata(
        override val id: String,
        val watermark: String?,
        val lastIndexedAt: DateTime?
) : Identifiable