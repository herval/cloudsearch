package cloudsearch.search.clients.google

import cloudsearch.Config
import cloudsearch.db.Account
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.gmail.Gmail
import com.google.gdata.client.contacts.ContactsService

/**
  * Created by herval on 7/28/15.
  */
object GoogleApis {
  val applicationName = "cloudsearch-dev"
  val jsonFactory = JacksonFactory.getDefaultInstance
  val httpTranspot = GoogleNetHttpTransport.newTrustedTransport()

  def credential(account: Account) = {
    new GoogleCredential().setAccessToken(account.token)
  }

  def gmailClient(account: Account) = {
    new Gmail.Builder(GoogleApis.httpTranspot, GoogleApis.jsonFactory, credential(account))
        .setApplicationName(GoogleApis.applicationName)
        .build()
  }

  def googleDriveClient(account: Account) = {
    val credential = new GoogleCredential().setAccessToken(account.token)
    new Drive.Builder(GoogleApis.httpTranspot, GoogleApis.jsonFactory, credential)
        .setApplicationName(GoogleApis.applicationName)
        .build()

  }

  def googleContactsClient(account: Account) = {
    val credential = new GoogleCredential().setAccessToken(account.token)
    val client = new ContactsService(GoogleApis.applicationName)
    client.setOAuth2Credentials(credential)
    client
  }

  def googleCredential(account: Account, refreshToken: String) = {
    new GoogleCredential.Builder().
        setJsonFactory(GoogleApis.jsonFactory).
        setTransport(GoogleApis.httpTranspot).
        setClientSecrets(Config.googleClientId, Config.googleClientSecret).
        build().
        setAccessToken(account.token).
        setRefreshToken(refreshToken)
  }

}
