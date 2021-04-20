package cloudsearch.auth

import cloudsearch.account.AccountType
import cloudsearch.clients.http.ClientBuilder
import cloudsearch.clients.http.HttpClient
import cloudsearch.clients.http.PostEncoding
import cloudsearch.clients.http.ResponseParser
import cloudsearch.config.Credentials
import cloudsearch.storage.AccountConfig
import cloudsearch.storage.idFor
import cloudsearch.util.Parsers
import com.google.gson.Gson

abstract class StandardOauth2Auth(
        val server: OauthRedirectConfig,
        val accountType: AccountType,
        val scopes: List<String>? = null,
        val authorizePath: String,
        val accessTokenPath: String,
        val credentials: Credentials,
        val jsonParser: Gson = Parsers.camelCased,
        val tokenParamsEncoding: PostEncoding = PostEncoding.FormUrlEncoded,
        val builder: (Map<*, *>) -> AccountConfig
): OAuthAuthenticator {

    private val redirectUri = server.redirectUrl(accountType)

    open fun paramsForAuthorize() = mapOf(
            "client_id" to credentials.clientId,
            "redirect_uri" to redirectUri,
            "response_type" to "code"
    ).plus(
            if (scopes != null) {
                mapOf(
                        "scope" to scopes.joinToString(" ")
                )
            } else {
                emptyMap()
            }
    )

    open fun paramsForToken(code: String): Map<String, String> {
        return mapOf(
                "client_id" to credentials.clientId,
                "client_secret" to credentials.clientSecret,
                "code" to code,
                "grant_type" to "authorization_code",
                "redirect_uri" to redirectUri
        )
    }

    private val client = HttpClient("", jsonParser, credentials = null)
    private val parser = ResponseParser(Map::class.java, jsonParser)

    override fun authorizeUrl(): String {
        return "${authorizePath}?${ClientBuilder.urlEncode(paramsForAuthorize())}"
    }

    abstract suspend fun postAccessToken(partialAccount: AccountConfig, data: Map<*, *>): AccountConfig?

    override suspend fun credentialsFromCode(code: String): AccountConfig {
        val res = client.post(
                accessTokenPath,
                params = paramsForToken(code),
                encoding = tokenParamsEncoding
        )

        val data = parser.getAndExtract(res)

        val error = data?.get("error")
        if (data == null || error != null) {
            throw IllegalStateException("Couldn't auth: ${error}")
        }

        val acct = builder(data).copy(active = false)

        val me = postAccessToken(acct, data)
        if (me != null) {
            return me.copy(
                    id = idFor(me.type, me.originalId), // regenerate the id to pick up partial changes
                    active = true
            )
        } else {
            throw IllegalArgumentException("Must fetch details")
        }
    }
}