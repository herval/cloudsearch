package cloudsearch.search

object Highlighter {
    // add html tags and stuff
    fun formatTags(text: String?): String? {
        if(text == null) {
            return null
        }

        return text.replace("\n", "<br>")
    }

    fun highlightTokens(text: String?, tokens: List<String>): String? {
        if(text == null) {
            return null
        }

        val highlightRanges = mutableListOf<Pair<Int, Int>>()

        tokens.forEach {
            var p = firstMatchIgnoreCase(text, it)
            while (p != null) {
                highlightRanges.add(p)
                p = firstMatchIgnoreCase(text, it, p.second + 1)
            }
        }

        val finalPairs = mutableSetOf<Pair<Int, Int>>()
        highlightRanges.forEach { r ->
            val overlaps = highlightRanges.filter { r2 ->
                r2 != r && (
                        (r2.first <= r.first && r2.second >= r.first) ||
                                (r2.first >= r.first && r2.first <= r.second)
                        )
            }
            if (overlaps.isNotEmpty()) {
//                println("${r} overlaps with ${overlaps}")
                finalPairs.add(
                        overlaps.map { it.first }.plus(r.first).min()!! to overlaps.map { it.second }.plus(r.second).max()!!
                )
            } else {
                finalPairs.add(r)
            }
        }

//        println(finalPairs)

        var finalText: String = text
        finalPairs
                .sortedByDescending { it.first } // revert so we can safely highlight in place
                .forEach {
                    val snip = finalText.substring(it.first, it.second)
                    finalText = finalText.replaceRange(
                            it.first,
                            it.second,
                            "<b>${snip}</b>"
                    )
                }

        return finalText
    }

    private fun firstMatchIgnoreCase(src: String, what: String, start: Int = 0): Pair<Int, Int>? {
        val length = what.length
        if (length == 0) {
            return null // no match
        }

        val firstLo = what[0].toLowerCase()
        val firstUp = what[0].toUpperCase()

        for (i in start until src.length) {
            // Quick check before calling the more expensive regionMatches() method:
            val ch = src[i]
            if (ch != firstLo && ch != firstUp) {
                continue
            }


            if (src.regionMatches(i, what, 0, length, ignoreCase = true)) {
                return i to i + what.length
            }
        }

        return null
    }

}