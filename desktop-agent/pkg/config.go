package cloudsearch

type Config struct {
	Env              Env
	AccountsStorage  AccountsStorage
	SearchEngine     *SearchEngine
	ResultsStorage   ResultsStorage
	FavoritesStorage FavoritesStorage
	AuthService      OAuth2Authenticator
	Registry         *Registry
	MetadataStorage  MetadataStorage
	ThumbnailHandler ThumbnailHandler
	ResultIndexer    Indexer
}
