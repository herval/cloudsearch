package cloudsearch.clients.google

import cloudsearch.clients.http.OfflineException
import cloudsearch.config.Credentials
import cloudsearch.content.ContentType
import cloudsearch.content.Files
import cloudsearch.search.ContentSearchable
import cloudsearch.search.InvolvementType
import cloudsearch.search.Query
import cloudsearch.storage.AccountConfig
import cloudsearch.util.TimeFormat
import com.google.api.services.drive.model.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.produce
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.net.UnknownHostException


class GoogleDriveSearch(
        val account: AccountConfig,
        val creds: Credentials
) : ContentSearchable {
    val logger = LoggerFactory.getLogger(javaClass.name)

    override val contentKinds = Files.fileAndFolderTypes

    override suspend fun search(query: Query, scope: CoroutineScope) = scope.produce {
        logger.debug("Searching gdrive")
        try {
            val results = client
                    .files()
                    .list()
                    .setQ(buildQuery(query))
                    .setMaxResults(20)
                    .execute()

            logger.debug("gdrive searched")

            results.items?.map { f ->
                // TODO only not explicitlyTrashed?
                logger.debug(f.toString())
                send(
                        Files.resultFor(
                                originalId = f.id,
                                title = f.originalFilename ?: f.title,
                                path = pathFor(f),
                                extension = f.fileExtension,
                                mimeType = f.mimeType,
                                sizeBytes = f.fileSize,
                                timestamp = DateTime(f.modifiedDate.value).millis,
                                thumbnail = f.thumbnailLink,
                                permalink = "https://drive.google.com/${folderOrFile(f)}/d/${f.id}/view",
                                accountId = account.id,
                                accountType = account.type,
                                involvesMe = query.involving.contains(InvolvementType.Me),
                                labels = f.labels?.keys?.toList()
                        )
                )
            }
        } catch (e: UnknownHostException) {
            throw OfflineException(e)
        }
    }

    private fun pathFor(f: File): String {
        return f.title
    }

    private fun folderOrFile(f: File): String {
        // TODO differentiate a folder from a file for preview
        return "file"
    }

    private fun buildQuery(query: Query): String {
        var q = "fullText contains '${query.text}' and '${account.username}' in readers"

        // modifiedTime modifiedTime > '2012-06-04T12:00:00'
        // owners writers readers in
        if (query.after != null) {
            " and modifiedTime > '${formatted(query.after)}'"
        }
        if (query.before != null) {
            " and modifiedTime < '${formatted(query.before)}'"
        }

        if (query.contentTypes != null) {
            val types = mutableListOf<String>()

            if (query.contentTypes.contains(ContentType.Image)) {
                types.add("(mimeType contains 'image') or (mimeType contains 'drawing')")
            }

            if (query.contentTypes.contains(ContentType.Video)) {
                types.add("(mimeType contains 'video')")
            }

            if (query.contentTypes.contains(ContentType.Folder)) {
                types.add("(mimeType contains 'folder')")
            }

            if (query.contentTypes.contains(ContentType.Document)) {
                types.add("(mimeType contains 'document')")
            }

            if (query.contentTypes.contains(ContentType.File)) {
                types.add("(mimeType contains 'file')")
            }

            q += " and (${types.joinToString(" or ")})"
        }

        logger.debug("Searching GDrive: ${q}")

        return q
    }

    private fun formatted(ts: DateTime): String {
        return TimeFormat.yyyyMmDdTHhMmSs.print(ts)
    }

    private val client = GoogleApis.googleDriveClient(account, creds)
}