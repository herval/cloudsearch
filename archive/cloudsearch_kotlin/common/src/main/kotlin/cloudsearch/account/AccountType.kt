package cloudsearch.account

import cloudsearch.storage.AccountConfig


enum class AccountType {
    Dropbox,
    Github,
    Google,
    Slack,
    Jira,
    Confluence
}

object Account {
    private fun sanitized(serverPath: String): String {
        return if (serverPath.startsWith("https://")) {
            serverPath
        } else {
            "https://${serverPath}"
        }
    }

    fun confluence(
            username: String,
            credentialsHash: String,
            serverPath: String
    ) = AccountConfig(
            originalId = "${username} @ ${serverPath}",
            username = username,
            credentials = credentialsHash,
            apiServer = sanitized(serverPath),
            type = AccountType.Confluence
    )

    fun google(
            username: String,
            credentials: String,
            refreshCredentials: String,
            scopes: List<String>,
            expiresAt: Long?
    ) = AccountConfig(
            originalId = username,
            username = username,
            credentials = credentials,
            refreshCredentials = refreshCredentials,
            expiresAt = expiresAt,
            scopes = scopes,
            type = AccountType.Google
    )

    fun dropbox(
            credentials: String,
            username: String,
            name: String
    ) = AccountConfig(
            originalId = name,
            username = username,
            expiresAt = null,
            credentials = credentials,
            type = AccountType.Dropbox
    )

    fun jira(
            username: String,
            credentialsHash: String,
            apiServer: String
    ) = AccountConfig(
            originalId = "${username} @ ${apiServer}",
            username = username,
            credentials = credentialsHash,
            apiServer = sanitized(apiServer),
            type = AccountType.Jira
    )

    fun slack(
            credentials: String,
            username: String,
            groupName: String
    ) = AccountConfig(
            groupName = groupName,
            originalId = username,
            username = username,
            credentials = credentials,
            type = AccountType.Slack
    )

    fun github(
            credentials: String,
            username: String
    ) = AccountConfig(
            originalId = username,
            username = username,
            credentials = credentials,
            type = AccountType.Github
    )

}