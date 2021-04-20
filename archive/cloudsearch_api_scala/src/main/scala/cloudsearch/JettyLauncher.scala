package cloudsearch

import cloudsearch.websockets.SearchWebsocketServlet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ DefaultServlet, ServletContextHandler }
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object JettyLauncher {
  def main(args: Array[String]) {
    val port = if(System.getenv("PORT") != null) System.getenv("PORT").toInt else 8080

    val server = new Server(port)
    val context = new WebAppContext()
    context.setContextPath("/")
    context.setResourceBase("src/main/webapp")

    // register the regular API routes
    context.setInitParameter(ScalatraListener.LifeCycleKey, "cloudsearch.scalatra.Routes")
    context.setEventListeners(Array(new ScalatraListener))

    server.setHandler(context)

    // register the websocket endpoint
    context.addServlet(classOf[SearchWebsocketServlet], "/search/interactive")

    server.start
    server.join
  }
}