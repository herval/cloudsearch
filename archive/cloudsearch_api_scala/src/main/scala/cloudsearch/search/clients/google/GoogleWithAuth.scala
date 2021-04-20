package cloudsearch.search.clients.google

import cloudsearch.db.Account
import cloudsearch.search.ResultPage
import cloudsearch.search.clients.google.contacts.GoogleContactsClient
import cloudsearch.search.clients.google.drive.GoogleDriveClient
import cloudsearch.search.clients.google.gmail.GmailClient
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.concurrent.Future

/**
  * Refreshes expired tokens as needed, then calls apis
  *
  * Created by herval on 7/30/15.
  */
class GoogleWithAuth(account: Account) {

  import scala.concurrent.ExecutionContext.Implicits.global

  def searchMulti(query: String): Future[List[Future[ResultPage]]] = {
    refreshToken.map { updatedAccount =>
      List(
        new GmailClient(updatedAccount).search(query),
        new GoogleDriveClient(updatedAccount).search(query),
        new GoogleContactsClient(updatedAccount).search(query)
      )
    }.recover {
      case e =>
        // TODO mark account as borked?
        logger.error(e.getMessage, e)
        List.empty

    }
  }

  private def refreshToken: Future[Account] = Future {
    val refreshedAccount = for {
      refreshToken <- account.refreshToken
      tokenExpiration <- account.tokenExpiresAt if tokenExpiration.isBefore(DateTime.now())
      updatedAccount <- updateToken(refreshToken)
    } yield updatedAccount

    refreshedAccount.getOrElse {
      account // nothin' changed yay
    }
  }

  private def updateToken(refreshToken: String) = {
    println("Reauth needed...")
    val credential = GoogleApis.googleCredential(account, refreshToken)
    credential.refreshToken()

    Account.update(
      account,
      credential.getAccessToken,
      credential.getRefreshToken,
      credential.getExpirationTimeMilliseconds
    )
  }


  private val logger = LoggerFactory.getLogger(this.getClass)

}
