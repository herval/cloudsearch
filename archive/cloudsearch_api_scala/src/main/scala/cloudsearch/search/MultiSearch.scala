package cloudsearch.search

import java.util.concurrent.TimeUnit

import cloudsearch.db.{Account, DropboxAccount, GoogleAccount}
import cloudsearch.search.clients.dropbox.DropboxClient
import cloudsearch.search.clients.google.GoogleWithAuth
import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by herval on 7/26/15.
  */
class MultiSearch(accounts: List[Account]) {

  import scala.concurrent.ExecutionContext.Implicits.global

  def all(query: String): List[Future[ResultPage]] = {
    accounts.collect {
      case account: DropboxAccount => List(
        new DropboxClient(account).search(query)
      )

      case account: GoogleAccount =>
        Await.result( // TODO timeout Google results without blocking
          new GoogleWithAuth(account).searchMulti(query),
          Duration(20, TimeUnit.SECONDS)
        )
    }.flatten
  }

  private val logger = LoggerFactory.getLogger(this.getClass)
}
