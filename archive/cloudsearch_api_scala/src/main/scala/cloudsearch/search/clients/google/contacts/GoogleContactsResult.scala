package cloudsearch.search.clients.google.contacts

import cloudsearch.search.Result

/**
  * Created by herval on 2/10/16.
  */
case class GoogleContactsResult(id: String,
                                name: Option[String],
                                organizations: List[String],
                                emails: List[String],
                                phones: List[String],
                                timestamp: Long,
                                kind: String = "contact",
                                source: String = "google_contacts") extends Result {
}
