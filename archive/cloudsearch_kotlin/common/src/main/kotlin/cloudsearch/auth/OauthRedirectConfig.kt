package cloudsearch.auth

import cloudsearch.account.AccountType

interface OauthRedirectConfig {
    fun redirectUrl(type: AccountType): String
}