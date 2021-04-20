package cloudsearch.auth

import cloudsearch.storage.AccountConfig
import kotlinx.coroutines.CoroutineScope

interface Authenticator

interface OAuthAuthenticator : Authenticator {
    fun authorizeUrl(): String
    suspend fun credentialsFromCode(code: String): AccountConfig
}


interface PasswordAuthenticator : Authenticator {
    suspend fun fromCredentials(username: String, password: String, server: String, scope: CoroutineScope): AccountConfig?
}
