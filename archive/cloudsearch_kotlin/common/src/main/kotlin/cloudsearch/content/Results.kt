package cloudsearch.content

import cloudsearch.account.AccountType
import org.joda.time.DateTime

object Results {

    private fun indexable(vararg contents: String?): String {
        return contents.joinToString(" ").toLowerCase()
    }

    fun event(
            originalId: String,
            title: String,
            accountId: String,
            body: String?,
            calendarId: String,
            calendarEmail: String,
            location: String?,
            timezone: String?,
            organizer: String?,
            attendees: List<String>,
            createdAt: Long,
            startAt: Long?,
            endAt: Long?,
            accountType: AccountType,
            permalink: String,
            involvesMe: Boolean = true
    ) = Result(
            originalId = originalId,
            title = title,
            timestamp = startAt ?: createdAt,
            accountId = accountId,
            body = body,
            details = mapOf(
                    "calendarEmail" to calendarEmail,
                    "startMillis" to startAt,
                    "endMillis" to endAt,
                    "location" to location,
                    "timezone" to timezone,
                    "organizer" to organizer,
                    "attendees" to attendees,
                    "when" to "${DateTime(startAt).toString()} - ${DateTime(endAt).toString()}", // TODO format better
                    "summary" to body,
                    "calendarId" to calendarId
            ),
            accountType = accountType,
            permalink = permalink,
            involvesMe = involvesMe,
            type = ContentType.Event,
            fullText = indexable(title, body, location)
    )

    fun calendar(
            originalId: String,
            title: String,
            body: String?,
            permalink: String?,
            accountId: String,
            timestamp: Long,
            accountType: AccountType,
            involvesMe: Boolean
    ) = Result(
            originalId = originalId,
            title = title,
            body = body,
            permalink = permalink,
            accountId = accountId,
            timestamp = timestamp,
            accountType = accountType,
            involvesMe = involvesMe,
            type = ContentType.Calendar,
            fullText = indexable(title, body),
            details = emptyMap()
    )

    fun image(
            originalId: String,
            path: String,
            title: String,
            timestamp: Long,
            permalink: String,
            body: String?,
            accountId: String,
            accountType: AccountType,
            sizeBytes: Long?,
            thumbnail: String?,
            labels: List<String>?,
            involvesMe: Boolean
    ) = Result(
            originalId = originalId,
            title = title,
            body = body,
            permalink = permalink,
            accountId = accountId,
            thumbnail = thumbnail,
            timestamp = timestamp,
            accountType = accountType,
            involvesMe = involvesMe,
            labels = labels ?: emptyList(),
            type = ContentType.Image,
            fullText = indexable(title, path),
            details = mapOf(
                    "sizeBytes" to sizeBytes,
                    "path" to path
            )
    )

    fun document(
            originalId: String,
            path: String,
            title: String,
            timestamp: Long,
            permalink: String,
            body: String?,
            accountId: String,
            accountType: AccountType,
            sizeBytes: Long?,
            thumbnail: String?,
            labels: List<String>?,
            involvesMe: Boolean
    ) = Result(
            originalId = originalId,
            title = title,
            body = body,
            permalink = permalink,
            accountId = accountId,
            thumbnail = thumbnail,
            timestamp = timestamp,
            accountType = accountType,
            involvesMe = involvesMe,
            labels = labels ?: emptyList(),
            type = ContentType.Document,
            fullText = indexable(title, path),
            details = mapOf(
                    "sizeBytes" to sizeBytes,
                    "path" to path
            )
    )

    fun video(
            originalId: String,
            path: String,
            title: String,
            timestamp: Long,
            permalink: String,
            body: String?,
            accountId: String,
            accountType: AccountType,
            sizeBytes: Long?,
            thumbnail: String?,
            labels: List<String>?,
            involvesMe: Boolean
    ) = Result(
            originalId = originalId,
            title = title,
            body = body,
            permalink = permalink,
            accountId = accountId,
            thumbnail = thumbnail,
            timestamp = timestamp,
            accountType = accountType,
            involvesMe = involvesMe,
            labels = labels ?: emptyList(),
            type = ContentType.Video,
            fullText = indexable(title, path),
            details = mapOf(
                    "sizeBytes" to sizeBytes,
                    "path" to path
            )
    )

    fun file(
            originalId: String,
            path: String,
            title: String,
            timestamp: Long,
            permalink: String,
            body: String?,
            accountId: String,
            accountType: AccountType,
            sizeBytes: Long?,
            thumbnail: String?,
            labels: List<String>?,
            involvesMe: Boolean
    ) = Result(
            originalId = originalId,
            title = title,
            body = body,
            permalink = permalink,
            accountId = accountId,
            thumbnail = thumbnail,
            timestamp = timestamp,
            accountType = accountType,
            involvesMe = involvesMe,
            labels = labels ?: emptyList(),
            type = ContentType.File,
            fullText = indexable(title, path),
            details = mapOf(
                    "sizeBytes" to sizeBytes,
                    "path" to path
            )
    )

    fun folder(
            originalId: String,
            path: String,
            title: String,
            timestamp: Long,
            permalink: String,
            accountId: String,
            accountType: AccountType,
            thumbnail: String?,
            labels: List<String>? = null,
            involvesMe: Boolean
    ) = Result(
            originalId = originalId,
            title = title,
            body = null,
            permalink = permalink,
            accountId = accountId,
            thumbnail = thumbnail,
            timestamp = timestamp,
            accountType = accountType,
            labels = labels ?: emptyList(),
            involvesMe = involvesMe,
            type = ContentType.Folder,
            fullText = indexable(title, path),
            details = mapOf(
                    "path" to path
            )
    )

    fun post(
            originalId: String,
            permalink: String,
            accountId: String,
            title: String,
            body: String,
            accountType: AccountType,
            timestamp: Long,
            involvesMe: Boolean
    ) = Result(
            originalId = originalId,
            title = title,
            body = body,
            permalink = permalink,
            accountId = accountId,
            timestamp = timestamp,
            accountType = accountType,
            involvesMe = involvesMe,
            type = ContentType.Post,
            fullText = indexable(title, body),
            details = mapOf()
    )


    fun task(
            originalId: String,
            permalink: String,
            accountId: String,
            title: String,
            body: String?,
            accountType: AccountType,
            timestamp: Long,
            labels: List<String>?,
            state: TaskState,
            involvesMe: Boolean
    ) = Result(
            originalId = originalId,
            title = title,
            body = body,
            permalink = permalink,
            accountId = accountId,
            timestamp = timestamp,
            accountType = accountType,
            involvesMe = involvesMe,
            type = ContentType.Task,
            fullText = indexable(title, body),
            labels = labels ?: emptyList(),
            details = mapOf(
                    "state" to state.name
            )
    )

    fun message(
            originalId: String,
            permalink: String,
            accountId: String,
            title: String,
            body: String,
            accountType: AccountType,
            timestamp: Long,
            involvesMe: Boolean
    ) = Result(
            originalId = originalId,
            title = title,
            body = body,
            permalink = permalink,
            accountId = accountId,
            timestamp = timestamp,
            accountType = accountType,
            involvesMe = involvesMe,
            type = ContentType.Message,
            fullText = indexable(title, body),
            details = mapOf(
                    "messages" to body
            )
    )

    fun email(
            originalId: String,
            accountId: String,
            from: String,
            to: String,
            sourceEmail: String,
            body: String,
            fullBody: String,
            title: String,
            unread: Boolean,
            timestamp: Long,
            labels: List<String>?,
            accountType: AccountType,
            permalink: String,
            involvesMe: Boolean
    ) = Result(
            originalId = originalId,
            title = title,
            body = body,
            permalink = permalink,
            accountId = accountId,
            timestamp = timestamp,
            accountType = accountType,
            involvesMe = involvesMe,
            type = ContentType.Email,
            fullText = indexable(title, body, from),
            details = mapOf(
                    "email" to sourceEmail,
                    "from" to from,
                    "to" to to,
                    "subject" to title,
                    "body" to fullBody,
                    "labels" to labels,
                    "unread" to unread
            )
    )

    fun contact(
            originalId: String,
            name: String,
            emails: List<String>,
            companies: List<String>,
            jobs: List<String>,
            addresses: List<String>,
            phones: List<String>,
            thumbnail: String?,
            birthday: String?,
            accountId: String,
            accountType: AccountType,
            permalink: String?,
            timestamp: Long,
            involvesMe: Boolean
    ) = Result(
            originalId = originalId,
            title = name,
            body = listOf(
                    labeled("Email: ", emails),
                    labeled("Phone: ", phones)
            ).filterNotNull().joinToString("\n"),
            permalink = permalink,
            accountId = accountId,
            timestamp = timestamp,
            thumbnail = thumbnail,
            accountType = accountType,
            involvesMe = involvesMe,
            type = ContentType.Contact,
            fullText = indexable(
                    name,
                    emails.joinToString(" "),
                    phones.joinToString(" "),
                    jobs.joinToString { " " },
                    companies.joinToString { " " },
                    addresses.joinToString { " " }
            ),
            details = mapOf(
                    "name" to name,
                    "emails" to emails.filterNot { it.isBlank() },
                    "phones" to phones.filterNot { it.isBlank() },
                    "jobs" to jobs.filterNot { it.isBlank() },
                    "companies" to companies.filterNot { it.isBlank() },
                    "addresses" to addresses.filterNot { it.isBlank() },
                    "birthday" to birthday,
                    "photo" to thumbnail
            )
    )

    private fun labeled(label: String, list: List<String>?): String? {
        return if (list != null && list.isNotEmpty()) {
            "${label}: ${list.joinToString(", ")}"
        } else {
            null
        }
    }
//
//    data class ResultMeta(
//            val id: String,
//            val description: String
//    )

//    val searchable = mapOf(
////            Application,
//            ContentType.Document to ResultMeta(
//                    "document",
//                    "Documents"
//            ),
//            ContentType.Email to ResultMeta(
//                    "email",
//                    "E-mails"
//            ),
//            ContentType.Event to ResultMeta(
//                    "event",
//                    "Events"
//            ),
//            ContentType.File to ResultMeta(
//                    "file",
//                    "Files"
//            ),
//            ContentType.Folder to ResultMeta(
//                    "folder",
//                    "Folders"
//            ),
//            ContentType.Image to ResultMeta(
//                    "image",
//                    "Images"
//            ),
//            ContentType.Message to ResultMeta(
//                    "message",
//                    "Messages"
//            ),
//            ContentType.Post to ResultMeta(
//                    "post",
//                    "Posts"
//            ),
//            ContentType.Task to ResultMeta(
//                    "task",
//                    "Tasks"
//            ),
//            ContentType.Video to ResultMeta(
//                    "video",
//                    "Videos"
//            )
//    )

}