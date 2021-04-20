package cloudsearch.search.clients.google.contacts

import java.net.URL

import cloudsearch.clients.SearchClient
import cloudsearch.db.Account
import cloudsearch.search.ResultPage
import cloudsearch.search.clients.google.GoogleApis
import com.google.gdata.client.Query
import com.google.gdata.data.contacts.ContactFeed
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import scala.concurrent.Future

class GoogleContactsClient(account: Account) extends SearchClient {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def search(query: String): Future[ResultPage] = Future {
    logger.info("Searching gdrive: " + query)
    //    val fullQuery = s"fullText contains '${query}'"
    val feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full")
    val contactsQuery = new Query(feedUrl)
    contactsQuery.setMaxResults(50)
    contactsQuery.setFullTextQuery(query)
    val feed = client.getFeed(contactsQuery, classOf[ContactFeed])

    val contacts = feed.getEntries.iterator().map { contact =>

      val orgs: Option[List[String]] = Option(contact.getOrganizations).map(_.flatMap { o =>
        Option(o.getOrgName).map(_.getValue)
      }.toList)

      val name: Option[String] = Option(contact.getTitle).map(_.getPlainText).
          orElse[String](orgs.flatMap(_.headOption))

      GoogleContactsResult(
        contact.getId,
        name,
        orgs.getOrElse(List.empty),
        contact.getEmailAddresses.map(_.getAddress).toList,
        contact.getPhoneNumbers.map(_.getPhoneNumber).toList,
        contact.getUpdated.getValue
      )
    }.toList

    ResultPage(account, "google_contacts", contacts)
  }

  private val client = GoogleApis.googleContactsClient(account)
  private val logger = LoggerFactory.getLogger(this.getClass)
}
