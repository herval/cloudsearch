package bleve_test

import (
	"github.com/herval/cloudsearch-desktop-agent/pkg"
	"github.com/herval/cloudsearch-desktop-agent/pkg/storage/bleve"
	"github.com/herval/cloudsearch-desktop-agent/pkg/storage/storm"
	"github.com/herval/cloudsearch-desktop-agent/pkg/test"
	"testing"
)

func storages(t *testing.T) (*bleve.BleveResultStorage, cloudsearch.FavoritesStorage, cloudsearch.MetadataStorage) {
	index, err := bleve.NewIndex("./", "")
	if err != nil {
		t.Fatal(err)
	}

	meta, err := storm.NewMetadataStorage("./")
	if err != nil {
		t.Fatal(err)
	}

	favorites, err := storm.NewCachedFavoritesStorage("./")
	if err != nil {
		t.Fatal(err)
	}

	s := bleve.NewResultStorage(index, meta, favorites).(*bleve.BleveResultStorage)

	err = s.Truncate()
	// TODO fix
	//if err != nil {
	//	t.Fatal("Couldnt truncate ", err)
	//}

	return s, favorites, meta
}

func assertSave(r cloudsearch.Result, s cloudsearch.ResultsStorage, t *testing.T) *cloudsearch.Result {
	r, err := s.Save(r)
	if err != nil {
		t.Fatal("should save the result ", err, r)
	}
	return &r
}

func TestFavorite(t *testing.T) {
	s, f, m := storages(t)
	defer s.Close()
	defer m.Close()
	defer f.Close()
	r := assertSave(
		cloudsearch.Result{
			Favorited:   false,
			ContentType: cloudsearch.Message,
			OriginalId:  "3",
		},
		s, t,
	)

	if r, err := s.Get(r.Id); err != nil || r.Favorited {
		t.Fatal("Should find the unfavorited result ", err, r)
	}

	if fav, err := f.ToggleFavorite(r.Id); err != nil || !fav {
		t.Fatal("should toggle favorite ", err, fav)
	}

	if fav, err := f.IsFavorite(r.Id); err != nil || !fav {
		t.Fatal("should be faved ", err, fav)
	}

	if fav, err := s.AllFavorited(); err != nil || len(fav) != 1 || !fav[0].Favorited {
		t.Fatal("should be faved ", err, fav)
	}
}

func TestContentTypeQuery(t *testing.T) {
	s, f, m := storages(t)
	defer s.Close()
	defer f.Close()
	defer m.Close()

	assertSave(
		cloudsearch.Result{
			ContentType: cloudsearch.Image,
			OriginalId:  "1",
		},
		s, t,
	)

	assertSave(
		cloudsearch.Result{
			ContentType: cloudsearch.Contact,
			OriginalId:  "2",
		},
		s, t,
	)

	q := cloudsearch.ParseQuery("type:image", "", test.DefaultRegistry())
	if res, err := s.Search(q); err != nil || len(res) != 1 {
		t.Fatal("should find the image content only: ", res)
	}

	q = cloudsearch.ParseQuery("type:file", "", test.DefaultRegistry())
	if res, err := s.Search(q); err != nil || len(res) != 0 {
		t.Fatal("should find no content!")
	}

}
