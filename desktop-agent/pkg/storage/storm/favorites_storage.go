package storm

import (
	"github.com/asdine/storm"
	cloudsearch "github.com/herval/cloudsearch-desktop-agent/pkg"
)

type favoritesStorage struct {
	db    *storm.DB
	cache map[string]bool
}

func NewCachedFavoritesStorage(storagePath string) (cloudsearch.FavoritesStorage, error) {
	db, err := storm.Open(cloudsearch.FileAt(storagePath, "favorites.db"))
	if err != nil {
		return nil, err
	}

	res := make([]string, 0)
	err = db.All(&res)
	if err != nil && err != storm.ErrNotFound {
		return nil, err
	}

	cache := map[string]bool{}
	for _, i := range res {
		cache[i] = true
	}

	return &favoritesStorage{
		db:    db,
		cache: cache,
	}, nil
}

func (f favoritesStorage) AllFavoritedIds() ([]string, error) {
	var res []string
	for k, _ := range f.cache {
		res = append(res, k)
	}
	return res, nil
}

func (f *favoritesStorage) IsFavorite(resultId string) (bool, error) {
	return f.cache[resultId], nil
}

func (f *favoritesStorage) ToggleFavorite(resultId string) (bool, error) {
	if f.cache[resultId] {
		err := f.db.Set("favorites", resultId, false)
		if err != nil {
			return false, err
		}
		delete(f.cache, resultId)

		return false, err
	}

	err := f.db.Set("favorites", resultId, true)
	if err != nil {
		return false, err
	}
	f.cache[resultId] = true

	return true, err
}

func (f *favoritesStorage) Close() {
	f.db.Close()
}
