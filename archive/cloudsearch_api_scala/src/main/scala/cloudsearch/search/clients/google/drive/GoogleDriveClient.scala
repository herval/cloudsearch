package cloudsearch.search.clients.google.drive

import cloudsearch.clients.SearchClient
import cloudsearch.db.Account
import cloudsearch.search.ResultPage
import cloudsearch.search.clients.google.GoogleApis
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import scala.concurrent.Future

class GoogleDriveClient(account: Account) extends SearchClient {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def search(query: String): Future[ResultPage] = Future {
    logger.info("Searching gdrive: " + query)
    val fullQuery = s"fullText contains '${query}'"
    val results = client.files().list().setQ(fullQuery).setMaxResults(50).execute()

    if (results.getItems != null) {
      ResultPage(
        account,
        "google_drive",
        results.getItems.map { f =>
          // TODO only not explicitlyTrashed?
          logger.info(f.toString)
          GoogleDriveResult(
            f.getId,
            f.getTitle,
            if (f.getKind == "drive#file") "file" else "folder",
            f.getModifiedDate.getValue,
            f.getAlternateLink,
            f.getMimeType
          )
        }.toList
      )
    } else {
      ResultPage(account, "google_drive", List.empty)
    }
  }

  private val client = GoogleApis.googleDriveClient(account)
  private val logger = LoggerFactory.getLogger(this.getClass)
}
