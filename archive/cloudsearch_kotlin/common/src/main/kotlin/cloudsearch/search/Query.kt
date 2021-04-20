package cloudsearch.search

import cloudsearch.account.AccountType
import cloudsearch.content.ContentType
import cloudsearch.storage.AccountConfig
import org.joda.time.DateTime

data class Query(
        val fullText: String = "",
        val text: String = "", // text without special tags (usually what should be sent to search services)
        val searchMode: SearchMode = SearchMode.All,
        val orderBy: SearchOrder = SearchOrder.Default,
        val accounts: List<AccountConfig>? = null,
        val contentTypes: List<ContentType>? = null,
        val accountTypes: List<AccountType>? = null,
        val involving: List<InvolvementType> = listOf(InvolvementType.Me),
        val offset: Int = 0,
        val maxResults: Int = 100,
        val after: DateTime? = null,
        val before: DateTime? = null,
        val statuses: List<ResultStatus>? = null
        // TODO labels
        // TODO involving certain people
) {
    val tokens = Tokenizer.tokens(text)

    companion object {
//        private val log = LoggerFactory.getLogger(javaClass.simpleName)

        fun parsed(query: String?): Query? {
            if (query == null) {
                return null
            }

            return Tokenizer.tokens(query)
                    .map { token ->
                        Tokenizer.specialTags.entries.firstOrNull {
                            it.key.matches(token)
                        }?.let { r ->
                            val transformer = r.value
                            // BOOMMM
                            r.key.matchEntire(token)?.groupValues?.drop(1)?.take(2)?.let {
                                try {
                                    it[0] to transformer(it[1])
                                } catch (e: java.lang.IllegalArgumentException) { // malformed tags
                                    null
                                }
                            }
                        }
                    }.filterNotNull()
                    .fold(emptyMap<String, List<Any>>()) { acc, pair ->
                        // aggregate by key
                        acc.plus(
                                pair.first to acc.getOrDefault(pair.first, emptyList()).plus(pair.second).filterNotNull()
                        )
                    }.let {
                        Query(
                                fullText = query,
                                text = Tokenizer.stripSpecialTokens(query),
                                searchMode = it["mode"]?.let { it as List<SearchMode> }?.firstOrNull()
                                        ?: SearchMode.All,
                                orderBy = it["sort"] as? SearchOrder ?: SearchOrder.Default,
                                after = it["after"]?.let { it as List<DateTime> }?.firstOrNull(),
                                before = it["before"]?.let { it as List<DateTime> }?.firstOrNull(),
                                accountTypes = it["service"]?.let { it as List<AccountType> },
                                contentTypes = it["type"]?.let { it as List<ContentType> },
                                involving = it["involving"]?.let { it as? List<InvolvementType> }
                                        ?: listOf(InvolvementType.Me)
                        )
                    }
        }
    }
}