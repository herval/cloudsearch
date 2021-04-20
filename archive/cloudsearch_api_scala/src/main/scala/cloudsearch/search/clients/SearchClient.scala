package cloudsearch.clients

import cloudsearch.search.{ResultPage, Result}

import scala.concurrent.Future

/**
 * Created by herval on 7/28/15.
 */
trait SearchClient {

  def search(query: String): Future[ResultPage]

}
