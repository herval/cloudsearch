package cloudsearch.clients.github

import cloudsearch.account.Account
import cloudsearch.account.AccountType
import cloudsearch.auth.OauthRedirectConfig
import cloudsearch.auth.StandardOauth2Auth
import cloudsearch.config.Credentials
import cloudsearch.storage.AccountConfig
import cloudsearch.util.Parsers


class GithubAuth(
        creds: Credentials,
        server: OauthRedirectConfig
) : StandardOauth2Auth(
        accountType = AccountType.Github,
        authorizePath = "https://github.com/login/oauth/authorize",
        accessTokenPath = "https://github.com/login/oauth/access_token",
        scopes = listOf("user:email", "read:user", "read:org"),
        server = server,
        credentials = creds,
        jsonParser = Parsers.underscored,
        builder = {
            Account.github(
                    credentials = it.get("access_token") as String,
                    username = "NOT SET"
            )
        }
) {

    override fun paramsForToken(code: String): Map<String, String> {
        return mapOf(
                "code" to code,
                "client_id" to credentials.clientId,
                "client_secret" to credentials.clientSecret
        )
    }

    override suspend fun postAccessToken(partialAccount: AccountConfig, data: Map<*, *>): AccountConfig? {
        val userData = GithubUserFetch(partialAccount, jsonParser).me()

        return if (userData != null) {
            partialAccount.copy(
                    username = userData.login,
                    originalId = userData.nodeId
            )
        } else {
            null
        }
    }
}

