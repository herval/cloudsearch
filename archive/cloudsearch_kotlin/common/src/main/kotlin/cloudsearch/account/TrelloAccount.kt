//package cloudsearch.account
//
//import cloudsearch.storage.AccountConfig
//import cloudsearch.util.md5
//
//data class TrelloAccount(
//        val token: String,
//        val username: String,
//        val nodeId: String?, // for graphql
//        override var active: Boolean = true
//) : AccountConfig {
//    override val name = username
//    override val expiresAt: Long? = null
//    override val id: String = md5("trello_${username}")
//}