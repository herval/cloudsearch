package cloudsearch

import "time"

type ResultsStorage interface {
	Close()
	Save(result Result) (Result, error)  // fully override a result
	Merge(result Result) (Result, error) // save a result, merging data such as favorite status if it's set
	Search(query Query) ([]Result, error)
	Get(resultId string) (*Result, error)

	FindOlderThan(maxTime time.Time) (<-chan Result, error)
	DeleteAllFromAccount(accountId string) ([]string, error)
	Delete(resultId string) error

	AllFavorited() ([]Result, error)
}

type FavoritesStorage interface {
	AllFavoritedIds() ([]string, error)
	IsFavorite(resultId string) (bool, error)
	ToggleFavorite(resultId string) (bool, error)
	Close()
}

type MetadataStorage interface {
	// latest sync token, date, element id, etc - service-dependent identifier of where last full index stopped
	WatermarkFor(accountId string, searchableId string) (string, error)
	SetWatermarkFor(accountId string, searchableId string, watermark string) error
	ClearWatermarks() error

	SaveThumbnail(searchableId string, data []byte) error
	GetThumbnail(searchableId string) ([]byte, error)
	DeleteThumbnail(searchableId string) error
	Close()
}
