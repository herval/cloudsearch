package cloudsearch.search.clients.google.drive

import cloudsearch.search.Result

/**
  * Created by herval on 2/10/16.
  */
case class GoogleDriveResult(id: String,
                             name: String,
                             kind: String,
                             timestamp: Long,
                             viewLink: String,
                             mimeType: String,
                             source: String = "google_drive") extends Result {
}
