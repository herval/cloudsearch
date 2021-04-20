package cloudsearch.clients.slack

import cloudsearch.account.Account
import cloudsearch.account.AccountType
import cloudsearch.auth.OauthRedirectConfig
import cloudsearch.auth.StandardOauth2Auth
import cloudsearch.clients.http.PostEncoding
import cloudsearch.config.Credentials
import cloudsearch.storage.AccountConfig
import cloudsearch.util.Parsers

class SlackAuth(
        creds: Credentials,
        server: OauthRedirectConfig
) : StandardOauth2Auth(
        accountType = AccountType.Slack,
        scopes = listOf("search:read"),
        server = server,
        authorizePath = "https://slack.com/oauth/authorize",
        accessTokenPath = "https://slack.com/api/oauth.access",
        tokenParamsEncoding = PostEncoding.FormUrlEncoded,
        credentials = creds,
        jsonParser = Parsers.camelCased,
        builder = {
            Account.slack(
                    credentials = it.get("access_token") as String,
                    username = "NOT SET",
                    groupName = "NOT SET"
            )
        }
) {

    suspend override fun postAccessToken(
            partialAccount: AccountConfig,
            data: Map<*, *>
    ): AccountConfig? {
        val userId = data.get("user_id") as? String
        val team = data.get("team_name") as? String

        if (userId == null || team == null) {
            return null
        }

        return partialAccount.copy(
                originalId = userId,
                username = "${userId} @ ${team}",
                groupName = team
        )
    }
}