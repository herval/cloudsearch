package cloudsearch.search.clients.google.gmail

import cloudsearch.search.Result

/**
  * Created by herval on 2/10/16.
  */
case class GmailResult(id: String,
                       snippet: String,
                       timestamp: Long,
                       source: String = "gmail") extends Result {
}
