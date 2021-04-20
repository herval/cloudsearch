package cloudsearch.storage

import cloudsearch.account.Account
import cloudsearch.account.AccountType
import cloudsearch.config.Config
import cloudsearch.embedded.storage.AccountConfigDiskStorage
import cloudsearch.embedded.storage.StorageConfig
import cloudsearch.util.md5
import junit.framework.TestCase

/**
 * Created by herval on 12/3/17.
 */
class StorageTest : TestCase() {

    val accounts = AccountConfigDiskStorage(
            StorageConfig.fromConfig(Config(".env.test"))
    )

    fun testAccounts() {
        accounts.clear()

        val d = Account.dropbox("token", "foo", "email")

        assertEquals(md5("dropbox_foo"), d.id)
        val f = d.copy(username = "bar")
        assertEquals(md5("dropbox_bar"), f.id)

        accounts.saveOrUpdate(d)
        accounts.saveOrUpdate(d) // subsequent saves with the same id don't create new instances
        accounts.saveOrUpdate(d.copy(credentials = "modified"))

        assertEquals(1, accounts.all().size)
        assertEquals("modified", (accounts.get(d.id))?.credentials)

        assert(
                accounts.all(accountTypes = listOf(AccountType.Google)).size == 0
        )

        accounts.delete(d)
        assertEquals(0, accounts.all().size)
    }

}