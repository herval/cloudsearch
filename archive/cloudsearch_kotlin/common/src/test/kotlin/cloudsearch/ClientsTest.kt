package cloudsearch

import cloudsearch.account.AccountType
import cloudsearch.auth.OauthRedirectConfig
import cloudsearch.clients.google.GoogleAuth
import cloudsearch.clients.google.GoogleContactsFetch
import cloudsearch.config.Credentials
import junit.framework.TestCase

/**
 * Created by herval on 12/3/17.
 */
class GoogleAuthTest : TestCase() {
    val googleAuth = GoogleAuth(
            Credentials("foo", "bar", "secret"),
            { GoogleContactsFetch(it) },
            object : OauthRedirectConfig {
                override fun redirectUrl(type: AccountType): String {
                    return "http://localhost/foo"
                }
            }
    )

    fun testGoogleAuthUrl() {
        assertNotNull(googleAuth.authorizeUrl())
    }
}