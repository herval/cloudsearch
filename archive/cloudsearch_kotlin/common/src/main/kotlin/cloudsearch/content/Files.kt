package cloudsearch.content

import cloudsearch.account.AccountType


object Files {

    val fileTypes = listOf(
            ContentType.File,
            ContentType.Image,
            ContentType.Video,
            ContentType.Document
    )

    val fileAndFolderTypes = fileTypes + listOf(ContentType.Folder)

    fun isFileType(c: ContentType): Boolean {
        return fileTypes.contains(c)
    }

    fun resultFor(
            originalId: String,
            path: String,
            title: String,
            extension: String? = null,
            mimeType: String? = null,
            timestamp: Long,
            permalink: String,
            sizeBytes: Long?,
            body: String? = null,
            accountId: String,
            accountType: AccountType,
            thumbnail: String? = null,
            involvesMe: Boolean,
            labels: List<String>? = null
    ): Result {
        val klass = kindFor(mimeType, extension, path)

        return when (klass) {
            ContentType.Image -> Results.image(originalId, path, title, timestamp, permalink, body, accountId, accountType, sizeBytes, thumbnail, labels = labels, involvesMe = involvesMe)
            ContentType.Video -> Results.video(originalId, path, title, timestamp, permalink, body, accountId, accountType, sizeBytes, thumbnail, labels = labels, involvesMe = involvesMe)
            ContentType.Document -> Results.document(originalId, path, title, timestamp, permalink, body, accountId, accountType, sizeBytes, thumbnail, labels = labels, involvesMe = involvesMe)
            ContentType.Folder -> Results.folder(originalId, path, title, timestamp, permalink, accountId, accountType, thumbnail, labels = labels, involvesMe = involvesMe)
            else -> {
                // generic fie
                Results.file(originalId, path, title, timestamp, permalink, body, accountId, accountType, sizeBytes, thumbnail, labels = labels, involvesMe = involvesMe)
            }
        }
    }


    private val image = listOf("png", "jpg", "jpeg", "gif", "bmp", "svg")
    private val video = listOf("mpg", "mkv", "avi", "mp4")

    fun contentKindForExtension(fileType: String): ContentType? {
        val canonical = if (fileType.startsWith(".")) {
            fileType.drop(1).toLowerCase()
        } else {
            fileType.split(".").last().toLowerCase()
        }

        return when (canonical) {
            in image -> ContentType.Image
            in video -> ContentType.Video
            else -> null
        }
    }

    // try mime, then extension, then the full path
    private fun kindFor(mimeType: String?, fileExtension: String?, path: String): ContentType? {
        return mimeType?.let {
            if (mimeType.contains("image") || mimeType.contains("drawing")) {
                ContentType.Image
            } else if (mimeType.contains("video")) {
                ContentType.Video
            } else if (mimeType.contains("folder")) {
                ContentType.Folder
            } else if (mimeType.contains("document")) {
                ContentType.Document
            } else {
                null
            }
        } ?: fileExtension?.let {
            contentKindForExtension(it)
        } ?: contentKindForExtension(path) ?: ContentType.File
    }
}