package cloudsearch.clients.google

import cloudsearch.config.Credentials
import cloudsearch.content.*
import cloudsearch.index.IndividualFetchable
import cloudsearch.storage.AccountConfig
import com.google.api.services.gmail.model.Thread
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GmailFetch(
        val account: AccountConfig,
        val creds: Credentials
) : IndividualFetchable {
    override val contentKinds = listOf(ContentType.Email) + Files.fileTypes

    val logger: Logger = LoggerFactory.getLogger(javaClass.name)

    private suspend fun client() = GoogleApis.gmailClient(account, creds)

    override suspend fun fetch(id: String): Result? {
        val t = client().users().threads().get("me", id).setFormat("full").execute()

        return if (t != null) {
            resultFor(account, t)
        } else {
            null
        }
    }

    private fun resultFor(config: AccountConfig, thread: Thread): Result {
        val message = thread.messages?.find { it.id == thread.id }

        val labels = message?.labelIds
        val subject = message?.payload?.headers?.firstOrNull { it.name == "Subject" }?.value ?: "no title"
        val from = message?.payload?.headers?.firstOrNull { it.name == "From" }?.value ?: ""
        val to = message?.payload?.headers?.firstOrNull { it.name == "Delivered-To" }?.value ?: ""
        val timestamp = (message?.get("internalDate") as? String)?.toLong() ?: 0L
        val snippet = message?.snippet ?: thread.messages?.firstOrNull { it.snippet != null }?.snippet ?: ""
        val plainMsg = message?.payload?.parts?.firstOrNull { it.get("mimeType") == "text/plain" }?.body?.decodeData()?.let { String(it) }
//        val htmlMsg = message?.payload?.parts?.firstOrNull { it.get("mimeType") == "text/html" }?.body?.decodeData()?.let { String(it) }

        return Results.email(
                originalId = thread.id,
                accountId = config.id,
                from = from,
                to = to,
                sourceEmail = config.username,
                body = snippet,
                fullBody = plainMsg ?: snippet,
                title = subject,
                unread = labels?.contains("UNREAD") ?: false,
                timestamp = DateTime(timestamp).millis,
                labels = labels,
                accountType = account.type,
                permalink = "https://mail.google.com/mail/u/${config.username}/#inbox/${thread.id}",
                involvesMe = true
        )
    }

}