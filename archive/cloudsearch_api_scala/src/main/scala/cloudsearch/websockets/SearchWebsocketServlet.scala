package cloudsearch.websockets

import org.eclipse.jetty.websocket.servlet.{WebSocketServlet, WebSocketServletFactory}

class SearchWebsocketServlet extends WebSocketServlet {

  @Override
  def configure(factory: WebSocketServletFactory) {
    factory.register(classOf[SearchWebsocket])
  }

}
