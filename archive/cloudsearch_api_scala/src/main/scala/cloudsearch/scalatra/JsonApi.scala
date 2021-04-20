package cloudsearch.scalatra


import org.scalatra.json.JacksonJsonSupport
import org.scalatra.{AsyncResult, BadGateway, FutureSupport}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

trait JsonApi extends AppStack with FutureSupport with JacksonJsonSupport {
  protected implicit def executor: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  before() {
    contentType = "application/json"
  }

  def async(block: => Any) = {
    try {
      new AsyncResult {
        override val is = Future {
            block
        }.recover {
          case e =>
            logger.error(e.getMessage, e)
            BadGateway()
        }
      }
    } catch {
      case e: Exception =>
        logger.error(e.getMessage, e)
        BadGateway()
    }


  }

  private val logger = LoggerFactory.getLogger(this.getClass)

}