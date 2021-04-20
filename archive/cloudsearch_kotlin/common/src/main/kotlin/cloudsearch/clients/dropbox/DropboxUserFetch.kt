package cloudsearch.clients.dropbox

import cloudsearch.clients.http.ClientBuilder
import cloudsearch.clients.http.OauthBearer
import cloudsearch.storage.AccountConfig
import cloudsearch.util.Parsers

class DropboxUserFetch(
        val account: AccountConfig
) {
    private val client = ClientBuilder.build(
            "https://api.dropboxapi.com",
            Parsers.underscored,
            Result::class,
            OauthBearer(account.credentials)
    )

    data class Result(
            val email: String
    )


    suspend fun userEmail(): String {
        return client.post(
                "/2/users/get_current_account",
                emptyMap(),
                null, // no body on this post
                null
        )?.email ?: throw IllegalStateException("Dropbox account should have an email!")
    }

}