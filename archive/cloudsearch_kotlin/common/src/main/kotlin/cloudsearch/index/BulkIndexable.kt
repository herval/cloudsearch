package cloudsearch.index

import cloudsearch.content.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import org.joda.time.Duration
import kotlin.coroutines.CoroutineContext

// for implementations that dont support search, use this to index locally
interface BulkIndexable {

    val updateInterval: Duration

    // optional watermark to support incremental fetching
    // onDone is a callback to get the new watermark #hack
    suspend fun fetchAll(watermark: String?, onDone: (String?) -> Unit, scope: CoroutineScope): ReceiveChannel<Result>

}