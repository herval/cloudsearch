package cloudsearch

import cloudsearch.account.AccountType
import cloudsearch.config.Config
import cloudsearch.storage.AccountConfig
import cloudsearch.util.base64
import org.joda.time.DateTime

object Accounts {
    val conf = Config(".env.test")
    val confluenceAccount = AccountConfig(
            originalId = "id",
            username = "jiratest",
            credentials = base64("jiratest:jiratest123"),
            apiServer = "https://cwiki.apache.org/confluence",
            type = AccountType.Confluence
    )

    val dropboxAccount = AccountConfig(
            credentials = "ZpoNflULPTAAAAAAAAAACz7Goly18uTYtVXv2MgAeyyl1XnpLCyCqjEGgDmMyW5x", // hervaltest1 account
            originalId = "hervaltest1",
            username = "hervaltest@hervalicio.us",
            active = true,
            type = AccountType.Dropbox
    )

    val jiraAccount = AccountConfig(
            originalId = "id",
            username = "jiratest",
            credentials = base64("jiratest:jiratest123"),
            apiServer = "https://issues.apache.org/jira",
            type = AccountType.Jira
    )

    var googleAccount = AccountConfig(
            username = "hervaltest1@gmail.com",
            originalId = "hervaltest1@gmail.com",
            credentials = "TOKEN",
            refreshCredentials = "1/3Uw42NLc8om44KVkOfyTlwrmyChJCQo0mM5IIRZKZL_zEyAmN_rAMj54TLi1n6IT", // hervaltest1 account
            scopes = listOf("wat"),
            expiresAt = DateTime.now().minusMinutes(1).millis,
            type = AccountType.Google
    )

    val githubAccount = AccountConfig(
            credentials = "522819535f84d0326f9d8e017e448d74b35c4053", // hervaltest account,
            username = "hervaltest",
            originalId = "hervaltest",
            active = true,
            type = AccountType.Github
    )

}