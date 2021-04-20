package cloudsearch.clients.google

import cloudsearch.config.Credentials
import cloudsearch.storage.AccountConfig
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.gmail.Gmail
import com.google.api.services.people.v1.PeopleService


/**
 * Created by hfreire on 1/8/17.
 */
object GoogleApis {
    val jsonFactory = JacksonFactory.getDefaultInstance()
    val httpTranspot = GoogleNetHttpTransport.newTrustedTransport()

    private fun credential(account: AccountConfig): GoogleCredential {
        return GoogleCredential().setAccessToken(token(account))
    }

    fun gmailClient(account: AccountConfig, creds: Credentials): Gmail {
        return Gmail.Builder(httpTranspot, jsonFactory, credential(account))
                .setApplicationName(creds.appName)
                .build()
    }

    fun googleDriveClient(account: AccountConfig, creds: Credentials): Drive {
        val credential = GoogleCredential().setAccessToken(token(account))
        return Drive.Builder(httpTranspot, jsonFactory, credential)
                .setApplicationName(creds.appName)
                .build()

    }

    fun googleContactsClient(account: AccountConfig): PeopleService {
        val credential = GoogleCredential().setAccessToken(token(account))
        val client = PeopleService.Builder(httpTranspot, jsonFactory, credential(account))
                .build()
        return client
    }

    private fun token(config: AccountConfig): String? {
        return config.credentials
    }
}