package cloudsearch.scalatra.routes

import cloudsearch.scalatra.AppStack

class BaseRoutes extends AppStack {

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
      </body>
    </html>
  }

}
