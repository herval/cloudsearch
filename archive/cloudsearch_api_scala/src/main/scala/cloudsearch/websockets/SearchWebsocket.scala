package cloudsearch.websockets

import cloudsearch.db.Account
import cloudsearch.search.{MultiSearch, ResultPage}
import cloudsearch.websockets.requests.SearchCommand
import cloudsearch.websockets.responses.{SearchEnded, SearchEvent, SearchResponse, SearchStarted}
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.{OnWebSocketClose, OnWebSocketConnect, OnWebSocketMessage, WebSocket}
import org.slf4j.LoggerFactory

import scala.concurrent.Future

@WebSocket
class SearchWebsocket {

  import org.json4s._
  import org.json4s.jackson.JsonMethods._
  import org.json4s.jackson.Serialization._

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val formats = DefaultFormats
  var userToken: Option[String] = None;

  @OnWebSocketMessage
  def onText(session: Session, message: String) = {
    val msg = parse(message).extract[SearchCommand]
    logger.info(msg.toString)

    msg match {
      case SearchCommand("search", token, query) => search(query, token, session)
      //      case SearchCommand("abort", query) => ???
    }
  }

  @OnWebSocketConnect
  def onConnect(session: Session) = {
    logger.info("Connect: " + session.toString)
  }

  @OnWebSocketClose
  def onClose(session: Session, status: Int, reason: String) = {
    logger.info("Close: " + session.toString)
  }

  def search(query: String, token: String, session: Session) = {
    val accts = Account.forUser(token)

    sendEvent(
      SearchEvent(
        "search_started",
        SearchStarted(accts.map(_.service))
      ),
      session
    )

    val searches = new MultiSearch(accts).all(query)

    // TODO cancel the pool if connection closes

    searches.foreach { f =>
      f.onSuccess {
        case results: ResultPage =>
          sendEvent(
            SearchEvent(
              "service_results",
              SearchResponse(query, results.account.service, results.resultType, results.results)
            ),
            session
          )

          sendEvent(
            SearchEvent(
              "search_ended",
              SearchEnded(results.account.service)
            ),
            session
          )
      }

      f.onFailure {
        case e =>
          logger.error(e.getMessage, e)
        //          sendEvent(
        //            SearchEvent(
        //              "search_failed",
        //              SearchEnded(results.account.service)
        //            ),
        //            session
        //          )
      }
    }

    Future.sequence(searches).onComplete {
      case _ => logger.info("All done!")
    }
  }

  private def sendEvent(e: SearchEvent, session: Session) = {
    val res = write(e)
    session.getRemote.sendString(res)
  }

  private val logger = LoggerFactory.getLogger(this.getClass)
}
