package search

import (
	"github.com/herval/cloudsearch-desktop-agent/pkg"
	"github.com/herval/cloudsearch-desktop-agent/pkg/search/bleve"
	"github.com/sirupsen/logrus"
)

func NewCachedSearchableBuilder(
	results cloudsearch.ResultsStorage,
	remotes cloudsearch.SearchableBuilder,
) cloudsearch.SearchableBuilder {
	return func(a cloudsearch.AccountData) ([]cloudsearch.SearchFunc, []string, error) {
		search, ids, err := remotes(a)
		if err != nil {
			logrus.Error("Could not setup a remote searchable for ", a.ID, " - ", err)
			search = []cloudsearch.SearchFunc{
				cloudsearch.NoopSearchable(),
			}
		}

		// support cached results
		cached := []cloudsearch.SearchFunc{}
		for i, c := range search {
			cs := bleve.NewIndexedResultsSearchable(results)
			ca := cloudsearch.NewCachedSearchable(ids[i], results, cs, c, a.AccountType)
			cached = append(cached, ca)
		}
		search = cached

		return search, ids, nil
	}
}

// a builder for the simple case: one searchable per account
func Builder(name string, search func(cloudsearch.AccountData) cloudsearch.SearchFunc) cloudsearch.SearchableBuilder {
	return func(account cloudsearch.AccountData) (fetchFns []cloudsearch.SearchFunc, ids []string, err error) {
		return []cloudsearch.SearchFunc{search(account)},
			[]string{name},
			nil
	}
}


func WithCaching(s cloudsearch.SearchableBuilder, enableCaching bool, results cloudsearch.ResultsStorage) cloudsearch.SearchableBuilder {
	if enableCaching {
		return NewCachedSearchableBuilder(results, s)
	}
	return s
}