package cloudsearch.search

import cloudsearch.account.AccountType
import cloudsearch.cache.*
import cloudsearch.clients.atlassian.ConfluenceSearch
import cloudsearch.clients.atlassian.JiraSearch
import cloudsearch.clients.dropbox.DropboxSearch
import cloudsearch.clients.github.GithubIssueSearch
import cloudsearch.clients.google.*
import cloudsearch.clients.http.OfflineException
import cloudsearch.clients.slack.SlackSearch
import cloudsearch.config.ServiceCredentials
import cloudsearch.content.ContentType
import cloudsearch.filter.*
import cloudsearch.index.CachedFetch
import cloudsearch.storage.AccountConfig
import cloudsearch.storage.FavoritesStorage
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

/**
 * Created by herval on 12/12/17.
 */
class SearchBuilder(
        val cache: ResultsCache,
        val googleAuthRefresh: GoogleAuthRefresh,
        val favorites: FavoritesStorage,
        val credentials: ServiceCredentials,
        val queriesCache: InMemoryStorage<DateTime> = InMemoryStorage<DateTime>()
) {
    private val logger = LoggerFactory.getLogger(javaClass.name)

    suspend fun all(
            accounts: List<AccountConfig>,
            mode: SearchMode,
            contentKinds: List<ContentType>?
    ): List<Searchable> {
        // one search on cache + one per downstream
        return when (mode) {
            SearchMode.Cache ->
                listOf(cacheSearch())
            SearchMode.Live ->
                liveSearches(accounts, contentKinds)
            else ->
                listOf(cacheSearch()) + liveSearches(accounts, contentKinds)
        }
    }

    private suspend fun liveSearches(accounts: List<AccountConfig>, contentKinds: List<ContentType>?): List<ContentSearchable> {
        return accounts.map {
            when (it.type) {
                AccountType.Google -> {
                    // since these objects are short-lived, we only need to refresh auth here/once
                    val acc = try {
                        googleAuthRefresh.refreshToken(it)
                    } catch (e: OfflineException) {
                        logger.debug("Offline, not refreshing")
                        it
                    }

                    listOf(
                            gmail(acc),
                            googleDrive(acc),
                            contacts(acc),
                            calendarEvents(acc)
                    )
                }
                AccountType.Dropbox -> listOf(
                        dropbox(it)
                )
                AccountType.Github -> listOf(
                        githubIssues(it)
                )
                AccountType.Slack -> listOf(
                        slackMessages(it)
                )
                AccountType.Jira -> listOf(
                        jiraSearch(it)
                )
                AccountType.Confluence -> listOf(
                        confluenceSearch(it)
                )
            }
        }.flatten().filter {
            // don't even search if source can't serve content kinds
            canServe(it.contentKinds, contentKinds)
        }
    }

    private fun cacheSearch(): Searchable {
        return cache
    }

    fun filters(q: Query): List<Filter> {
        return listOf(
                FilterNotInRange(q), // return only contents in specified range
                Dedup(q), // don't propagate results found on cache & live api twice
                FilterContent(q), // filter content to specific types
                LimitResults(q) // stop downstream when maxResults is reached
        )
    }

    fun jiraSearch(account: AccountConfig) = updateCache(
            JiraSearch(account)
    )(account)

    fun confluenceSearch(account: AccountConfig) = updateCache(
            ConfluenceSearch(account)
    )(account)

    private fun slackMessages(account: AccountConfig) = updateCache(
            SlackSearch(account)
    )(account)

    private fun gmailFetch(account: AccountConfig) = CachedFetch(
            cache,
            GmailFetch(account, credentials.get(AccountType.Google))
    )

    private fun gmail(account: AccountConfig) = updateCache(
            GmailSearch(account, gmailFetch(account), credentials.get(AccountType.Google)),
            FetchToRefresh(account, cache, gmailFetch(account))
    )(account)

    private fun googleDrive(account: AccountConfig) = updateCache(
            GoogleDriveSearch(account, credentials.get(AccountType.Google))
    )(account)

    private fun calendarEvents(account: AccountConfig) = updateCache(
            GoogleCalendarEventSearch(account, cache)
    )(account)

    private fun contacts(account: AccountConfig) = SearchAndCache(
            account,
            cache,
            liveSearch = ContentSearchable.identity(account, listOf(ContentType.Contact)), // no live-search for contacts, only cache
            refresher = FetchToRefresh(account, cache, GoogleContactsFetch(account)),
            queriesCache = queriesCache
    )

    private fun githubIssues(account: AccountConfig) = updateCache(
            GithubIssueSearch(account)
    )(account)

    fun dropbox(account: AccountConfig) = updateCache(
            DropboxSearch(account)
    )(account)

    private fun updateCache(
            c: ContentSearchable,
            refresher: Refreshable? = null
    ): (AccountConfig) -> SearchAndCache {
        return { a: AccountConfig ->
            SearchAndCache(
                    a,
                    cache,
                    c,
                    refresher ?: ExpireCached(a, cache, favorites),
                    queriesCache
            )
        }
    }

    // do setA and setB intersect at all?
    private fun canServe(setA: List<ContentType>, setB: List<ContentType>?): Boolean {
        return (setB == null || setA.any { setB.contains(it) })
    }


}