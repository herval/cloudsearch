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

// for Jira, confluence, etc
class JiraAuth(val search: SearchBuilder) : PasswordAuthenticator {
    private val log = LoggerFactory.getLogger(javaClass)
    suspend override fun fromCredentials(username: String, password: String, server: String, scope: CoroutineScope): AccountConfig? {
        try {
            val hash = base64("${username}:${password}")
            val account = Account.jira(
                    username,
                    hash,
                    server
            )
            // get one result to validate the account
            return try {
                search.jiraSearch(account).liveSearch.search(
                        Query("foo", "foo", maxResults = 1, involving = listOf(InvolvementType.Me)),
                        scope
                ).receiveOrNull()
                account
            } catch (e: Exception) { // if api host doesn't exist will throw this
                log.debug("Couldn't save: ${e.message}")
                null
            }
        } catch (e: UnauthorizedException) {
            return null
        }
    }

}