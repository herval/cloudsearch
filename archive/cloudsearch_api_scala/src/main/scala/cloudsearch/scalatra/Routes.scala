package cloudsearch.scalatra

import javax.servlet.ServletContext

import cloudsearch.scalatra.routes.{SearchRoutes, BaseRoutes}
import org.scalatra._

class Routes extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new BaseRoutes, "/*")
    context.mount(new SearchRoutes, "/search/*")
  }
}
