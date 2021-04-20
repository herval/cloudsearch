package cloudsearch.db

import java.util.Date

import cloudsearch.db.MainDatabaseContext._
import org.joda.time.DateTime
import scalikejdbc._

trait Account {
  val id: Int
  val userId: Int
  val service: String
  val token: String
  val refreshToken: Option[String] = None
  val tokenExpiresAt: Option[DateTime] = None

}

object Account extends SQLSyntaxSupport[Account] {
  override val tableName = "accounts"

  def apply(rs: WrappedResultSet): Account = {
    val id = rs.int("id")
    val user = rs.int("user_id")
    val service = rs.string("service")
    val token = rs.string("token")
    val refreshToken = rs.string("refresh_token")
    val tokenExpiration = rs.jodaDateTime("token_expires_at")

    service match {
      case "dropbox_oauth2" =>
        DropboxAccount(id, user, token)
      case "google_oauth2" =>
        GoogleAccount(id, user, token, Option(refreshToken), Option(tokenExpiration))
      case other =>
        UnsupportedAccountType(id, user, token, service)
    }
  }


  def update(account: Account, accessToken: String, refreshToken: String, tokenExpiresAt: Long): Option[Account] = {
    val ts = new Date(tokenExpiresAt)
    sql"""
        update accounts set token=${accessToken}, refresh_token=${refreshToken}, token_expires_at=${ts}
            where id=${account.id}
      """.update.apply()
    Account.forId(account.id)
  }

  def forId(id: Int): Option[Account] = {
    sql"""
      select * from accounts where id=${id} and state='active'
    """.map(r => Account(r))
        .single
        .apply()
  }

  def forUser(userToken: String)(implicit s: DBSession = AutoSession): List[_ <: Account] = {
    sql"""
      select * from accounts
        inner join users on accounts.user_id=users.id
        where users.authentication_token=${userToken} and state='active'
    """.map(r => Account(r))
        .list
        .apply()
  }
}


case class UnsupportedAccountType(id: Int, userId: Int, token: String, service: String) extends Account {
}


case class DropboxAccount(id: Int,
                          userId: Int,
                          token: String) extends Account {
  override val service = "dropbox"
}

case class GoogleAccount(id: Int,
                         userId: Int,
                         token: String,
                         override val refreshToken: Option[String],
                         override val tokenExpiresAt: Option[DateTime]) extends Account {
  override val service = "google"
}