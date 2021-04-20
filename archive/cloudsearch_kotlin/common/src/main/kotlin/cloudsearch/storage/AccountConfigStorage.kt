package cloudsearch.storage

import cloudsearch.account.AccountType

interface AccountConfigStorage : Storage<AccountConfig> {
    fun all(accountTypes: List<AccountType>? = null): List<AccountConfig>
    fun active(accountTypes: List<AccountType>? = null): List<AccountConfig>
}