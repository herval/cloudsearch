package cloudsearch.clients.google

import cloudsearch.clients.http.HttpClient
import cloudsearch.clients.http.OauthBearer
import cloudsearch.storage.AccountConfig
import cloudsearch.util.Parsers

object Google {
    val CALENDAR_READONLY = "https://www.googleapis.com/auth/calendar.readonly"
    val GMAIL_READONLY = "https://www.googleapis.com/auth/gmail.readonly"
    val DRIVE_READONLY = "https://www.googleapis.com/auth/drive.readonly"
    val USERINFO_EMAIL = "https://www.googleapis.com/auth/userinfo.email"
    val PLUS_LOGIN = "https://www.googleapis.com/auth/plus.login" // to get the user email
    val CONTACTS_READONLY = "https://www.googleapis.com/auth/contacts.readonly"


    fun client(account: AccountConfig): HttpClient {
        return HttpClient(
                "https://www.googleapis.com",
                parser = Parsers.camelCased,
                credentials = OauthBearer(account.credentials)
        )
    }
}