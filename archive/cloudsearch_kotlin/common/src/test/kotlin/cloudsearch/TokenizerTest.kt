package cloudsearch

import cloudsearch.account.AccountType
import cloudsearch.content.ContentType
import cloudsearch.search.Query
import cloudsearch.search.SearchMode
import cloudsearch.search.Tokenizer
import junit.framework.TestCase
import org.joda.time.DateTime

class TokenizerTest : TestCase() {

    fun testSpecialTokens() {
        val q = "notactually a tag, type:folder bu:tt.... before:2017-01-01 after:2017-01-01 @[type:file] service:google mode:Cache"
        // TODO test failed states
        val query = Query.parsed(q)!!
        assertEquals(query.before, DateTime.parse("2017-01-01"))
        assertEquals(query.after, DateTime.parse("2017-01-01"))
        assertEquals(query.contentTypes, listOf(ContentType.Folder, ContentType.File))
        assertEquals(query.accountTypes, listOf(AccountType.Google))
        assertEquals(query.searchMode, SearchMode.Cache)

        assertEquals(
                "notactually a tag,  bu:tt....",
                Tokenizer.stripSpecialTokens(q)
        )
    }

    fun testTokens() {
        assertEquals(
                Tokenizer.tokens("some \"quoted stuff\" and \" more        stuff"),
                listOf("quoted stuff", "some", "and", "more", "stuff")
        )

        assertEquals(
                Tokenizer.tokens("\"all quoted stuff\" \""),
                listOf("all quoted stuff")
        )
    }
}
