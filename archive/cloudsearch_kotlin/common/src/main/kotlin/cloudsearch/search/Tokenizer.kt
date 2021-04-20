package cloudsearch.search

import cloudsearch.account.AccountType
import cloudsearch.content.ContentType
import org.joda.time.DateTime

object Tokenizer {

    val specialTags = mapOf<Regex, ((String) -> Any?)>(
            Regex("""(before):([-\/0-9]+)""") to { str -> DateTime.parse(str) },
            Regex("""(after):([-\/0-9]+)""") to { str -> DateTime.parse(str) },
            Regex("""(service):([\w]+)""") to { str -> AccountType.valueOf(str.capitalize()) },
            Regex("""(mode):([\w]+)""") to { str -> SearchMode.valueOf(str.capitalize()) },
            Regex("""@\[(service):([\w]+)\]""") to { str -> AccountType.valueOf(str.capitalize()) },
            Regex("""@\[(type):([\w]+)\]""") to { str -> ContentType.valueOf(str.capitalize()) },
            Regex("""(type):([\w]+)""") to { str -> ContentType.valueOf(str.capitalize()) },
            Regex("""(involving):([\w]+)""") to { str -> InvolvementType.valueOf(str.capitalize()) }
    )

    fun stripSpecialTokens(query: String): String {
        return specialTags.keys.fold(query) { acc, tag ->
            tag.replace(acc, "")
        }.trim()
    }

    fun tokens(query: String): List<String> {
        val tokens = mutableListOf<String>()
        var q = query.trim()
        while (q.contains("\"")) {
            // extract the quoted strings as individual tokens first
            val first = q.indexOf('\"')
            var last = q.indexOf('\"', first + 1)
            if (last == -1) { // a lone quote remains
                last = Math.min(first + 1, q.length - 1)
            } else {
                val q1 = q.substring(first + 1, last)
                tokens.add(q1)
            }

            q = q.substring(0, first) + q.substring(last + 1)
            q = q.trim()
        }

        tokens.addAll(
                q.split(" ")
        )

        // ignore case and blanks
        return tokens
                .filter { it.isNotBlank() }
                .map { it.toLowerCase() }
    }
}