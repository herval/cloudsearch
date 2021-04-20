package cloudsearch.clients.google

import cloudsearch.account.Account
import cloudsearch.account.AccountType
import cloudsearch.auth.AuthRefresh
import cloudsearch.auth.OAuthAuthenticator
import cloudsearch.auth.OauthRedirectConfig
import cloudsearch.auth.UnauthorizedException
import cloudsearch.clients.http.OfflineException
import cloudsearch.config.Credentials
import cloudsearch.storage.AccountConfig
import cloudsearch.storage.idFor
import com.google.api.client.auth.oauth2.TokenResponseException
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import org.joda.time.DateTime
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

val ALL_GOOGLE_SCOPES = listOf(
        Google.CALENDAR_READONLY,
        Google.GMAIL_READONLY,
        Google.DRIVE_READONLY,
        Google.USERINFO_EMAIL,
        Google.PLUS_LOGIN,
        Google.CONTACTS_READONLY
)


class GoogleAuthRefresh(val creds: Credentials) : AuthRefresh {
    private fun credentials(account: AccountConfig): GoogleCredential {
        return credentialsClient(creds).setAccessToken(account.credentials).setRefreshToken(account.refreshCredentials)
    }

    override suspend fun refreshToken(account: AccountConfig): AccountConfig {
        val credential = credentials(account)
        try {
            return if (credential.refreshToken()) {
                account.copy(
                        credentials = credential.accessToken,
                        refreshCredentials = credential.refreshToken,
                        expiresAt = DateTime(credential.expirationTimeMilliseconds).millis
                )
            } else {
                account
            }
        } catch (e: TokenResponseException) {
            throw UnauthorizedException(account.originalId, e)
        } catch (e: ConnectException) {
            throw OfflineException(e)
        } catch (e: SocketException) {
            throw OfflineException(e)
        } catch (e: SocketTimeoutException) {
            throw OfflineException(e)
        } catch (e: UnknownHostException) {
            throw OfflineException(e)
        }
    }

}

class GoogleAuth(
        val creds: Credentials,
        val contactBuilder: (AccountConfig) -> GoogleContactsFetch,
        server: OauthRedirectConfig,
        val scopes: List<String> = ALL_GOOGLE_SCOPES
) : OAuthAuthenticator {
    private val client: GoogleAuthorizationCodeFlow = GoogleAuthorizationCodeFlow.Builder(
            NetHttpTransport(),
            JacksonFactory(),
            creds.clientId,
            creds.clientSecret,
            scopes
    ).setApprovalPrompt("force")
            .setAccessType("offline")
            .build()

    private val redirectUrl = server.redirectUrl(AccountType.Google)
//    private val redirectUrl = "urn:ietf:wg:oauth:2.0:oob"

    // https://stackoverflow.com/questions/12521385/how-to-authenticate-google-drive-without-requiring-the-user-to-copy-paste-auth-c
    override fun authorizeUrl(): String {
        return client.newAuthorizationUrl()
                .setRedirectUri(redirectUrl)
                .toString()
    }

    override suspend fun credentialsFromCode(code: String): AccountConfig {
        val response = client.newTokenRequest(code).setRedirectUri(redirectUrl).execute()
        val credential = credentialsClient(creds).setFromTokenResponse(response)

        val account = Account.google(
                username = "NOT SET",
                credentials = credential.accessToken,
                refreshCredentials = credential.refreshToken,
                scopes = scopes,
                expiresAt = DateTime(credential.expirationTimeMilliseconds).millis
        )

        val me = contactBuilder(account).me()
        return if (me != null) {
            val email = me.emailAddresses.first().value
            account.copy(
                    originalId = email,
                    username = email
            )
        } else {
            throw IllegalArgumentException("Must fetch user id")
        }
    }
}

private fun credentialsClient(creds: Credentials): GoogleCredential {
    return GoogleCredential.Builder()
            .setJsonFactory(GoogleApis.jsonFactory)
            .setTransport(GoogleApis.httpTranspot)
            .setClientSecrets(creds.clientId, creds.clientSecret)
            .build()
}
