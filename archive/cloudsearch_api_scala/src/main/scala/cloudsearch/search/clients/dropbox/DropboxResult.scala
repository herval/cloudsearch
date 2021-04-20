package cloudsearch.search.clients.dropbox

import cloudsearch.search.Result

/**
  * Created by herval on 2/10/16.
  */
case class DropboxResult(path: String,
                         name: String,
                         kind: String,
                         timestamp: Long,
                         source: String = "dropbox") extends Result {
}
