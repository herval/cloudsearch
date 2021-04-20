package cloudsearch.clients.github

import cloudsearch.clients.http.HttpClient
import cloudsearch.clients.http.OauthToken
import cloudsearch.clients.http.ResponseParser
import cloudsearch.storage.AccountConfig
import cloudsearch.util.Parsers
import com.google.gson.Gson


class GithubUserFetch(account: AccountConfig, parser: Gson) {
    private val parser = ResponseParser(GithubUserData::class.java, parser)
    private val client = HttpClient(
            "https://api.github.com",
            parser = Parsers.underscored,
            credentials = OauthToken(account.credentials)
    )

    suspend fun me(): GithubUserData? {
        return parser.getAndExtract(
                client.get("/user")
        )
    }

    data class GithubUserData(val id: Int, val email: String, val login: String, val nodeId: String)
}

