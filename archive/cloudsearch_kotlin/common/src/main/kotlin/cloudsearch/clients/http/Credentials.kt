package cloudsearch.clients.http

sealed class Credentials
data class OauthBearer(val token: String) : Credentials()
data class OauthToken(val token: String) : Credentials()
data class Basic(val base64: String) : Credentials()
