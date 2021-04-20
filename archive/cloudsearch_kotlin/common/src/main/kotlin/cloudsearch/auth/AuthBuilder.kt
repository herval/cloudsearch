package cloudsearch.auth

import cloudsearch.account.AccountType
import cloudsearch.clients.atlassian.ConfluenceAuth
import cloudsearch.clients.atlassian.JiraAuth
import cloudsearch.clients.dropbox.DropboxAuth
import cloudsearch.clients.dropbox.DropboxUserFetch
import cloudsearch.clients.github.GithubAuth
import cloudsearch.clients.google.GoogleAuth
import cloudsearch.clients.google.GoogleContactsFetch
import cloudsearch.clients.slack.SlackAuth
import cloudsearch.config.ServiceCredentials
import cloudsearch.search.SearchBuilder
import cloudsearch.storage.AccountConfig
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

class AuthBuilder(
        val credentials: ServiceCredentials,
        val server: OauthRedirectConfig,
        val searchBuilder: SearchBuilder
) {

    private fun authenticatorFor(acc: AccountType): Authenticator {
        return when (acc) {
            AccountType.Google -> GoogleAuth(credentials.get(acc), { GoogleContactsFetch(it) }, server)
            AccountType.Dropbox -> DropboxAuth(credentials.get(acc), server, { DropboxUserFetch(it) })
            AccountType.Github -> GithubAuth(credentials.get(acc), server)
            AccountType.Slack -> SlackAuth(credentials.get(acc), server)
            AccountType.Jira -> JiraAuth(searchBuilder)
            AccountType.Confluence -> ConfluenceAuth(searchBuilder)
        }
    }

    val authenticators = mutableMapOf<AccountType, Authenticator>()

    private fun authFor(service: AccountType): Authenticator {
        return authenticators.get(service).let {
            if (it == null) {
                val auth = authenticatorFor(service)
                authenticators.put(service, auth)
                auth
            } else {
                it
            }
        }
    }

    suspend fun accountFor(service: String, code: String): AccountConfig {
        return (authFor(AccountType.valueOf(service)) as OAuthAuthenticator).credentialsFromCode(code)
    }

    fun authUrlFor(service: String): String {
        return (authFor(AccountType.valueOf(service)) as OAuthAuthenticator).authorizeUrl()
    }

    suspend fun accountForBasicAuth(service: String, username: String, password: String, server: String, scope: CoroutineScope): AccountConfig? {
        return (authFor(AccountType.valueOf(service)) as PasswordAuthenticator).fromCredentials(username, password, server, scope)
    }

}