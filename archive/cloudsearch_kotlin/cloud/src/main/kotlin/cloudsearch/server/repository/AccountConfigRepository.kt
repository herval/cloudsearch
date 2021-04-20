package cloudsearch.server.repository

import cloudsearch.account.AccountType
import cloudsearch.server.table.AccountConfigs
import cloudsearch.storage.AccountConfig
import cloudsearch.storage.AccountConfigStorage
import org.jetbrains.exposed.sql.deleteAll

class AccountConfigRepository(
        val table: AccountConfigs = AccountConfigs()
) : AccountConfigStorage {

    override fun all(accountTypes: List<AccountType>?): List<AccountConfig> {
        return table.all()
    }

    override fun active(accountTypes: List<AccountType>?): List<AccountConfig> {
        return all(accountTypes).filter { it.active }
    }

    override fun clear() {
        table.deleteAll()
    }

    override fun get(id: String): AccountConfig? {
        return table.get(id)
    }

    override fun saveOrUpdate(res: AccountConfig): Boolean {
        return table.saveOrUpdate(res)
    }

    override fun delete(res: AccountConfig): Boolean {
        return table.delete(res.id)
    }

    override fun getAll(): List<AccountConfig> {
        return all()
    }

    override fun deleteById(id: String): Boolean {
        return table.delete(id)
    }

    override fun deleteAllById(keys: List<String>) {
        keys.forEach {
            deleteById(it)
        }
    }

    override fun count(): Int {
        return all().size
    }
}