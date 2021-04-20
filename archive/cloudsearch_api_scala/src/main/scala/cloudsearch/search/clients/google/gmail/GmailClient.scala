package cloudsearch.search.clients.google.gmail

import cloudsearch.clients.SearchClient
import cloudsearch.db.Account
import cloudsearch.search.ResultPage
import cloudsearch.search.clients.google.GoogleApis
import com.google.api.services.gmail.model.{Thread => GmailThread}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.concurrent.Future

class GmailClient(account: Account) extends SearchClient {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def search(q: String): Future[ResultPage] = {

    Future {
      logger.info("Searching gmail: " + q)
      client.users().threads().list("me").setQ(q).setMaxResults(20l).execute()
    }.flatMap { results =>

      if (results.getThreads != null) {
        fetchDetails(results.getThreads.toList).map { results =>
          ResultPage(
            account,
            "gmail",
            results
          )
        }
      } else {
          Future.successful(
            ResultPage(account, "gmail", List.empty)
          )
      }
    }
  }

  private def fetchDetails(gmailThreads: List[GmailThread]): Future[List[GmailResult]] = {
    val fetches: List[Future[GmailResult]] = gmailThreads.map { t =>
      Future {
        val thread = client.users().threads().get("me", t.getId).execute()
        val timestamp = thread.getMessages.head.get("internalDate").asInstanceOf[String].toLong

        GmailResult(
          t.getId,
          t.getSnippet,
          timestamp
        )
      }
    }

    Future.sequence(fetches)
  }

  private val client = GoogleApis.gmailClient(account)
  private val logger = LoggerFactory.getLogger(this.getClass)
}