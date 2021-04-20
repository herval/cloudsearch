package bleve_test

import (
	"context"
	bl "github.com/blevesearch/bleve"
	"github.com/herval/cloudsearch-desktop-agent/pkg"
	"github.com/herval/cloudsearch-desktop-agent/pkg/search/bleve"
	bl1 "github.com/herval/cloudsearch-desktop-agent/pkg/storage/bleve"
	"log"
	"reflect"
	"strconv"
	"testing"
	"time"
)

var savedResults []cloudsearch.Result
var index bl.Index
var ts time.Time
var searchable cloudsearch.SearchFunc

func TestMain(m *testing.M) {
	env := cloudsearch.Env{
		StoragePath: "./",
	}
	dropb := cloudsearch.AccountData{
		ID:          "123",
		Token:       "dropboxtoken",
		AccountType: cloudsearch.Dropbox,
	}

	var err error
	index, err = bl1.NewIndex(env.StoragePath, strconv.Itoa(int(time.Now().Unix())))
	if err != nil {
		log.Fatal(err)
	}

	ts = time.Date(2018, 01, 01, 0, 0, 0, 0, time.UTC)

	savedResults = []cloudsearch.Result{
		{
			OriginalId:  "id1",
			Title:       "fooa",
			Body:        "bar bazzzzz",
			Permalink:   "fu.com",
			AccountId:   dropb.ID,
			Timestamp:   ts,
			AccountType: cloudsearch.Google,
			ContentType: cloudsearch.Document,
		},
		{
			OriginalId:  "id2",
			Title:       "fooo",
			Body:        "bar bazzzz",
			Permalink:   "fu.com",
			AccountId:   dropb.ID,
			Timestamp:   ts,
			AccountType: cloudsearch.Dropbox,
			ContentType: cloudsearch.Document,
		},
		{
			OriginalId:  "id3",
			Title:       "foo",
			Body:        "bar <b>baz</b>boz foo/bor/boz",
			Permalink:   "fu.com",
			AccountId:   dropb.ID,
			Timestamp:   ts,
			AccountType: cloudsearch.Dropbox,
			ContentType: cloudsearch.Image,
		},
	}

	storage := bl1.NewResultStorage(index, nil, nil)
	for i, r := range savedResults {
		savedResults[i], err = storage.Save(r)
		if err != nil {
			log.Fatal(err)
		}
	}

	searchable = bleve.NewIndexedResultsSearchable(storage)

	m.Run()
}

func TestSearchAllFilters(t *testing.T) {
	a := ts.Add(-time.Second * 10)
	b := ts.Add(time.Second * 10)
	res := searchable(cloudsearch.Query{
		Text:         "baz",
		After:        &a,
		Before:       &b,
		AccountTypes: []cloudsearch.AccountType{cloudsearch.Google},
		ContentTypes: []cloudsearch.ContentType{cloudsearch.Document},
	}, context.TODO())

	r := <-res
	r.CacheHitScore = 0 // this will be different, we know

	if !eq(r, savedResults[0]) {
		t.Fatal("Did not deserialize correctly:\n", r, "vs\n", savedResults[0])
	}
}

func eq(a, b cloudsearch.Result) bool {
	return a.Id == b.Id &&
		a.AccountId == b.AccountId &&
		a.AccountType == b.AccountType &&
		a.Title == b.Title &&
		a.Permalink == b.Permalink &&
		a.Thumbnail == b.Thumbnail &&
		a.Timestamp == b.Timestamp &&
		a.ContentType == b.ContentType &&
		reflect.DeepEqual(a.Details, b.Details) &&
		a.OriginalId == b.OriginalId &&
		a.Body == b.Body &&
		a.CachedAt == b.CachedAt &&
		reflect.DeepEqual(a.Labels, b.Labels) &&
		a.InvolvesMe == b.InvolvesMe &&
		a.Status == b.Status
}

func TestAfter(t *testing.T) {
	a := ts.Add(time.Second * 1)
	res := searchable(cloudsearch.Query{
		Text:  "baz",
		After: &a,
	}, context.TODO())

	var r *cloudsearch.Result
	for rr := range res {
		r = &rr
	}

	if r != nil {
		t.Fatal("Expected no content, found ", r)
	}
}

func TestHtmlTokenizing(t *testing.T) {
	res := searchable(cloudsearch.Query{
		Text: "baz boz", // should find although terms are separated with html tags
	}, context.TODO())

	r := <-res
	if !eq(r, savedResults[2]) {
		t.Fatal("Did not deserialize correctly:\n", r, "vs\n", savedResults[2])
	}
}

func TestSlashes(t *testing.T) {
	res := searchable(cloudsearch.Query{
		Text: "foo bor boz", // should find although terms are separated with slashes
	}, context.TODO())

	r := <-res
	if !eq(r, savedResults[2]) {
		t.Fatal("Did not deserialize correctly:\n", r, "vs\n", savedResults[2])
	}
}
