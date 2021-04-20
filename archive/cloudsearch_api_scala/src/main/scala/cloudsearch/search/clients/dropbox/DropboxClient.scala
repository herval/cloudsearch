package cloudsearch.search.clients.dropbox

import java.util.Locale

import cloudsearch.clients.SearchClient
import cloudsearch.db.Account
import cloudsearch.search.ResultPage
import com.dropbox.core._
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.concurrent.Future


class DropboxClient(account: Account) extends SearchClient {

  import scala.concurrent.ExecutionContext.Implicits.global

  val config = new DbxRequestConfig("cloudsearch-dev", Locale.getDefault().toString)

  lazy val dbxClient = new DbxClient(config, account.token)

  def thumbnailStream(path: String, rev: String) = {
    dbxClient.startGetThumbnail(
      DbxThumbnailSize.w640h480,
      DbxThumbnailFormat.JPEG,
      path,
      rev
    )
  }

  def shareableLink(path: String) = {
    dbxClient.createShareableUrl(path)
  }

  override def search(pattern: String): Future[ResultPage] = Future {
    logger.info("Searching dropbox: " + pattern)
    val entries = dbxClient.searchFileAndFolderNames("/", pattern).take(50)
    ResultPage(
      account,
      "dropbox",
      entries.map { e =>
        logger.info(e.toString)

        DropboxResult(
          e.path,
          e.name,
          if (e.isFile) "file" else "folder",
          if (e.isFile) e.asFile().lastModified.getTime else 0
        )
      }.toList
    )
  }


  private val logger = LoggerFactory.getLogger(this.getClass)
}
