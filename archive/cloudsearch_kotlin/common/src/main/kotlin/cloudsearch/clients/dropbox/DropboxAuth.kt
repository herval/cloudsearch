package cloudsearch.clients.dropbox

import cloudsearch.account.Account
import cloudsearch.account.AccountType
import cloudsearch.auth.OauthRedirectConfig
import cloudsearch.auth.StandardOauth2Auth
import cloudsearch.config.Credentials
import cloudsearch.storage.AccountConfig

class DropboxAuth(
        creds: Credentials,
        server: OauthRedirectConfig,
        val dropboxClient: (AccountConfig) -> DropboxUserFetch
) : StandardOauth2Auth(
        server = server,
        accountType = AccountType.Dropbox,
        authorizePath = "https://www.dropbox.com/oauth2/authorize",
        accessTokenPath = "https://api.dropboxapi.com/oauth2/token",
        credentials = creds,
        builder = { a ->
            Account.dropbox(
                    credentials = a.get("access_token") as String,
                    username = "NOT SET",
                    name = "NOT SET"
            )
        }
) {

    suspend override fun postAccessToken(
            partialAccount: AccountConfig,
            data: Map<*, *>
    ): AccountConfig? {
        val userId = data.get("account_id") as? String
        if (userId == null) {
            return null
        }

        return partialAccount.copy(
                username = dropboxClient(partialAccount).userEmail(),
                originalId = userId
        )
    }
}