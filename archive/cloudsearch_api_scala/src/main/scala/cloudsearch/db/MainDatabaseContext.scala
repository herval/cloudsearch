package cloudsearch.db

import cloudsearch.Config
import org.joda.time.DateTimeZone
import scalikejdbc._


object MainDatabaseContext {

  java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"))
  DateTimeZone.setDefault(DateTimeZone.UTC)


  Class.forName("org.postgresql.Driver")
  ConnectionPool.singleton(
    Config.mainDatabaseUrl,
    Config.mainDatabaseUser,
    Config.mainDatabasePassword
  )

  implicit val session = AutoSession

}