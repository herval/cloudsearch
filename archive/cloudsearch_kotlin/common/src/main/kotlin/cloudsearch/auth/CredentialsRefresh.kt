package cloudsearch.auth

import cloudsearch.storage.AccountConfig
import cloudsearch.storage.Storage
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by hfreire on 1/8/17.
 */
class CredentialsRefresh(
        var account: AccountConfig,
        val storage: Storage<AccountConfig>,
        val auth: AuthRefresh
) {

    val logger: Logger = LoggerFactory.getLogger(javaClass.name)

    suspend fun get(): AccountConfig {
        val expiration = account.expiresAt
        if (expiration != null && DateTime(expiration).isBefore(DateTime.now())) {
            logger.debug("Updating credentials for ${account.id}")
            val updated = runBlocking {
                updateToken(account)
            }
            storage.saveOrUpdate(updated)
            account = updated
        }

        return account
    }

    private suspend fun updateToken(account: AccountConfig): AccountConfig {
        logger.debug("Reauthing...")
        return auth.refreshToken(account)
    }

}