import com.mojolly.scalate.ScalatePlugin.ScalateKeys._
import org.scalatra.sbt.ScalatraPlugin


lazy val `core` = (project in file(".")).
    settings(ScalatraPlugin.scalatraWithJRebel: _*)
    .settings(scalateSettings: _*)
    .settings(
      name := "cloudsearch-api",
      version := "1.0",
      scalaVersion := "2.11.5",
      resolvers ++= Seq(
        Classpaths.typesafeReleases,
        "Clojars" at "http://clojars.org/repo",
        "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
      ),
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % "2.4.0.M3",
        "org.scalatra" %% "scalatra-scalate" % "2.4.0.M3",
        "org.scalatra" %% "scalatra-specs2" % "2.4.0.M3" % "test",
        "org.scalatra" %% "scalatra-json" % "2.4.0.M3",
        "org.json4s" %% "json4s-jackson" % "3.3.0.RC1",
        "ch.qos.logback" % "logback-classic" % "1.1.3",
        "org.eclipse.jetty" % "jetty-webapp" % "9.1.5.v20140505" % "compile;container",
        "org.eclipse.jetty.websocket" % "websocket-servlet" % "9.2.10.v20150310" % "container;provided",
        "org.eclipse.jetty.websocket" % "websocket-server" % "9.2.10.v20150310" % "compile;container;provided",
        "javax.servlet" % "javax.servlet-api" % "3.1.0",
        "com.dropbox.core" % "dropbox-core-sdk" % "[1.7,1.8)",
        "com.google.api-client" % "google-api-client" % "1.20.0",
        "com.google.oauth-client" % "google-oauth-client-jetty" % "1.20.0",
        "com.google.apis" % "google-api-services-gmail" % "v1-rev29-1.20.0",
        "com.google.apis" % "google-api-services-drive" % "v2-rev179-1.20.0",
        "com.google.apis" % "google-api-services-calendar" % "v3-rev132-1.20.0",
        "com.google.gdata" % "core" % "1.47.1",
        "com.restfb" % "restfb" % "1.14.0",
        "org.postgresql" % "postgresql" % "9.4-1200-jdbc41",
        "org.scalikejdbc" %% "scalikejdbc" % "2.2.7",
        "org.scalikejdbc" %% "scalikejdbc-interpolation" % "2.2.7"
      ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile) { base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty, /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ), /* add extra bindings here */
            Some("templates")
          )
        )
      }
    )

packageArchetype.java_application