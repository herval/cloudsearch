package cloudsearch.embedded.storage

import cloudsearch.account.AccountType
import cloudsearch.storage.AccountConfig
import cloudsearch.storage.AccountConfigStorage
import com.googlecode.cqengine.query.QueryFactory.all


/**
 * Created by herval on 12/12/17.
 */
class AccountConfigDiskStorage(
        config: StorageConfig
) : KeyValueStore<AccountConfig>(
        AccountConfig::class,
        "accounts",
        config,
        cleanupOnFail = true // TODO figure out a way to migrate those
), AccountConfigStorage {

    override fun all(accountTypes: List<AccountType>?): List<AccountConfig> {
        return data
                .retrieve(all(AccountConfig::class.java))
                .filter { it ->
                    (accountTypes == null || accountTypes.contains(it.type))
                }
                .toList()
    }

    override fun active(accountTypes: List<AccountType>?): List<AccountConfig> {
        return all(accountTypes)
                .filter { it.active }
    }

}
