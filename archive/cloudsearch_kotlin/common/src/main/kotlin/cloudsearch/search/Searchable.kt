package cloudsearch.search

import cloudsearch.content.ContentType
import cloudsearch.content.Result
import cloudsearch.storage.AccountConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce


interface ContentHolder {
    val contentKinds: List<ContentType>
}

/**
 * Created by hfreire on 1/8/17.
 */
interface Searchable {
    fun active(): Boolean = true
    suspend fun search(query: Query, scope: CoroutineScope): ReceiveChannel<Result>
}

interface ContentSearchable : Searchable, ContentHolder {

    companion object {
        fun identity(account: AccountConfig, contentKinds: List<ContentType> = emptyList()): ContentSearchable = object : ContentSearchable {
            override val contentKinds = contentKinds
            override suspend fun search(query: Query, scope: CoroutineScope): ReceiveChannel<Result> = scope.produce {}
            override fun active() = false
        }
    }
}
