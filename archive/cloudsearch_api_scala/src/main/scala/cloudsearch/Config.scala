package cloudsearch

/**
 * Created by herval on 7/25/15.
 */
object Config {

  val mainDatabaseUrl = sys.env.getOrElse("DATABASE_CONNECTION_URL", "FIXME")
  val mainDatabaseUser = sys.env.getOrElse("DATABASE_USER", "FIXME")
  val mainDatabasePassword = sys.env.getOrElse("DATABASE_PASSWORD", "FIXME")

  val googleClientId = sys.env.getOrElse("GOOGLE_CLIENT_ID", "FIXME")
  val googleClientSecret = sys.env.getOrElse("GOOGLE_CLIENT_SECRET", "FIXME")

}
