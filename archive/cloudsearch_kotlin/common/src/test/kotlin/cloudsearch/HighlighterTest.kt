package cloudsearch

import cloudsearch.search.Highlighter
import cloudsearch.search.Tokenizer
import junit.framework.TestCase

class HighlighterTest : TestCase() {

    fun testHighlighting() {
        assertEquals(
                "<b>Some</b> <b>\"quoted some stuff\"</b> and \" <b>some</b> more        stuff",
                Highlighter.highlightTokens(
                        "Some \"quoted some stuff\" and \" some more        stuff",
                        listOf("\"quoted some stuff\"", "some")
                )
        )
    }
}
