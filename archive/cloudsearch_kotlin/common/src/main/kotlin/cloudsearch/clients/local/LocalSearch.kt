//package cloudsearch.clients.local
//
//import cloudsearch.content.Files
//import cloudsearch.content.Result
//import cloudsearch.search.ContentSearchable
//import cloudsearch.search.Query
//import cloudsearch.storage.AccountConfig
//import kotlinx.coroutines.experimental.channels.ReceiveChannel
//
//class LocalSearch(
//        override val account: AccountConfig
//) : ContentSearchable {
//    override val contentKinds = listOf(Application::class) + Files.fileAndFolderTypes
//
//    suspend override fun search(query: Query): ReceiveChannel<Result> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//}