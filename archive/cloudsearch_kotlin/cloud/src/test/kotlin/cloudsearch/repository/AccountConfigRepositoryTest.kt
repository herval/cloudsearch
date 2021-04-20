//package cloudsearch.repository
//
//import cloudsearch.account.AccountType
//import cloudsearch.server.repository.AccountConfigRepository
//import cloudsearch.storage.AccountConfig
//import cloudsearch.storage.setupDb
//import junit.framework.TestCase
//import org.joda.time.DateTime
//
//class AccountConfigRepositoryTest : TestCase() {
//
//    val repo = AccountConfigRepository()
//
//    fun testWriteAndRead() {
//        setupDb()
//        val accs = listOf(
//                AccountConfig(
//                        credentials = "foo1",
//                        originalId = "foo1",
//                        username = "foo1",
//                        scopes = emptyList(),
//                        expiresAt = DateTime.now().millis,
//                        active = true,
//                        id = "1",
//                        type = AccountType.Google
//                ),
//                AccountConfig(
//                        credentials = "foo2",
//                        username = "foo2",
//                        originalId = "foo2",
//                        scopes = emptyList(),
//                        expiresAt = DateTime.now().millis,
//                        active = false,
//                        id = "2",
//                        type = AccountType.Google
//                ),
//                AccountConfig(
//                        credentials = "foo3",
//                        username = "foo3",
//                        originalId = "foo3",
//                        active = true,
//                        id = "3",
//                        type = AccountType.Dropbox
//                )
//        )
//        accs.forEach {
//            repo.saveOrUpdate(it)
//            repo.saveOrUpdate(it) // double-inserts work
//        }
//
//        repo.active(listOf(AccountType.Google))
//    }
//
//}