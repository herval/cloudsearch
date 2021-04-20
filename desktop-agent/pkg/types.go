package cloudsearch

type SearchType string

// logical "groupings" for the UI, so we can display saved searches, favorite lists, etc, as different groups of results
const (
	RegularSearch  SearchType = "search"
	Favorites      SearchType = "favorites"
	SavedSearches  SearchType = "saved_searches"
	RecentSearches SearchType = "recent_searches"
)

const (
	SearchQuery ContentType = "SearchQuery" // saved, recent, etc - a search query itself
)

type AccountType string

const (
	Dropbox    AccountType = "Dropbox"
	Google     AccountType = "Google"
	Github     AccountType = "Github"
	Slack      AccountType = "Slack"
	Jira       AccountType = "Jira"
	Confluence AccountType = "Confluence"
	Trello     AccountType = "Trello"
)
