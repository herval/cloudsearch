package cloudsearch.clients.atlassian

import cloudsearch.account.Account
import cloudsearch.auth.PasswordAuthenticator
import cloudsearch.auth.UnauthorizedException
import cloudsearch.search.InvolvementType
import cloudsearch.search.Query
import cloudsearch.search.SearchBuilder
import cloudsearch.storage.AccountConfig
import cloudsearch.util.base64
import kotlinx.coroutines.CoroutineScope
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext


// for Jira, confluence, etc
class ConfluenceAuth(val search: SearchBuilder) : PasswordAuthenticator {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend override fun fromCredentials(username: String, password: String, server: String, scope: CoroutineScope): AccountConfig? {
        try {
            val hash = base64("${username}:${password}")
            val account = Account.confluence(
                    username,
                    hash,
                    server
            )

            return try {
                search.confluenceSearch(account).liveSearch.search(
                        Query("foo", "foo", maxResults = 1, involving = listOf(InvolvementType.Me)),
                        scope
                )
                account
            } catch (e: Exception) {
                log.debug("Couldn't save: ${e.message}")
                null
            }
        } catch (e: UnauthorizedException) {
            return null
        }
    }

}