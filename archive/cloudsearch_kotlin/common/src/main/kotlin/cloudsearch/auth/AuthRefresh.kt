package cloudsearch.auth

import cloudsearch.storage.AccountConfig

interface AuthRefresh {
    suspend fun refreshToken(account: AccountConfig): AccountConfig
}