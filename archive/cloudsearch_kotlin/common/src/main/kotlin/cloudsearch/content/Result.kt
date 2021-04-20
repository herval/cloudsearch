package cloudsearch.content

import cloudsearch.account.AccountType
import cloudsearch.search.Highlighter.formatTags
import cloudsearch.search.Highlighter.highlightTokens
import cloudsearch.search.Query
import cloudsearch.storage.AccountConfig
import cloudsearch.storage.Identifiable
import cloudsearch.util.md5
import org.joda.time.DateTime


fun idFor(accountId: String, originalId: String, type: ContentType): String {
    return "${accountId}_${md5(originalId)}_${md5(type.name)}"
}

/**
 * Created by hfreire on 1/8/17.
 */
data class Result(
        val title: String,
        val originalId: String,
        val accountId: String,
        val accountType: AccountType,
        val body: String?,
        val cachedAt: Long = DateTime.now().millis,
        val permalink: String?,
        val thumbnail: String? = null,
        val timestamp: Long,
        val labels: List<String> = emptyList(),
        val favorited: Boolean = false,
        val involvesMe: Boolean = true, // little hack to differentiate involves:anyone from involves:me
        val fullText: String,
        val details: Map<String, Any?>,
        val type: ContentType,
        override val id: String = idFor(accountId, originalId, type)
) : Identifiable {


    fun relevance(): Long { // TODO content relevance depending on query?
        return when (type) {
            ContentType.Event -> {
                val ts = DateTime(timestamp)
                return standardRelevance() + if (ts.isAfterNow) {
                    if (ts.isBefore(DateTime.now().plusDays(2))) {
                        standardRelevance() * 10
                    } else if (ts.isBefore(DateTime.now().plusDays(5))) {
                        standardRelevance() * 2
                    } else {
                        standardRelevance() / 5
                    }
                } else {
                    standardRelevance() / 100
                }
            }
            ContentType.Contact -> standardRelevance() * 2   // contacts are always important...?
            ContentType.Folder -> standardRelevance() / 100

//            ContentType.Task -> // TODO non-closed scores higher, with deadline scores higher x2

            else -> standardRelevance()
        }
    }

    private fun standardRelevance(): Long {
        return timestamp / 100000000 + if (favorited) {
            10000000
        } else {
            0
        }
    }

    fun toJson(query: Query): Map<String, Any?> {
        val ts = DateTime(timestamp)
        return mapOf(
                "id" to id,
                "accountId" to accountId,
                "sourceType" to accountType,
                "title" to formatTags(highlightTokens(title, query.tokens)),
                "snippet" to formatTags(highlightTokens(body, query.tokens)),
                "kind" to type.name,
                "permalink" to permalink,
                "thumbnail" to thumbnail,
                "timestamp" to ts.toString(),
                "timestampMillis" to ts.millis,
                "order" to relevance(),
                "favorited" to favorited,
                "group" to "search_results",
                "previewable" to true,
                "details" to details
        )
    }
}