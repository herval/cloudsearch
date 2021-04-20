package config

import (
	google2 "github.com/herval/cloudsearch-desktop-agent/pkg/fetch/google"
	"github.com/herval/cloudsearch-desktop-agent/pkg/search"
	"github.com/herval/cloudsearch-desktop-agent/pkg/search/dropbox"
	"github.com/herval/cloudsearch-desktop-agent/pkg/search/google"
	"github.com/herval/cloudsearch-desktop-agent/pkg/search/slack"
	"net/http"
	"time"

	authgateway "github.com/herval/authgateway/client"
	"github.com/herval/cloudsearch-desktop-agent/pkg"
	"github.com/herval/cloudsearch-desktop-agent/pkg/auth"
	"github.com/herval/cloudsearch-desktop-agent/pkg/storage/bleve"
	"github.com/herval/cloudsearch-desktop-agent/pkg/storage/storm"
)

// entry-point for registering any new account types. This makes sure they have proper associated searchables and auth
func registerAll(
	r *cloudsearch.Registry,
	authService cloudsearch.OAuth2Authenticator,
	accounts cloudsearch.AccountsStorage,
	env cloudsearch.Env,
	results cloudsearch.ResultsStorage,
	enableCaching bool,
) {

	r.RegisterAccountType(
		cloudsearch.Dropbox,
		search.WithCaching(search.Builder("dropbox", dropbox.NewSearch), enableCaching, results),
		auth.Builder(dropbox.NewAuthenticator()),
		nil,
	)
	r.RegisterAccountType(
		cloudsearch.Google,
		search.WithCaching(google.SearchBuilder, enableCaching, results),
		google.AuthBuilder(authService, accounts, auth.OauthRedirectUrlFor(env, cloudsearch.Google)),
		google2.FetchableBuilder,
	)
	r.RegisterAccountType(
		cloudsearch.Slack,
		search.WithCaching(search.Builder("slack", slack.NewSearch), enableCaching, results),
		auth.Builder(slack.NewAuthenticator()),
		nil,
	)
	r.RegisterContentTypes(
		cloudsearch.Document,
		cloudsearch.Email,
		cloudsearch.File,
		cloudsearch.Folder,
		cloudsearch.Image,
		cloudsearch.Video,
		cloudsearch.Application,
		cloudsearch.Calendar,
		cloudsearch.Contact,
		cloudsearch.Event,
		cloudsearch.Message,
		cloudsearch.Post,
		cloudsearch.Task,
		cloudsearch.SearchQuery,
	)
}

func NewConfig(env cloudsearch.Env, enableCaching bool) (cloudsearch.Config, error) {
	accounts, err := storm.NewAccountsStorage(env.StoragePath)
	if err != nil {
		return cloudsearch.Config{}, err
	}

	meta, err := storm.NewMetadataStorage(env.StoragePath)
	if err != nil {
		return cloudsearch.Config{}, err
	}

	favorites, err := storm.NewCachedFavoritesStorage(env.StoragePath)
	if err != nil {
		return cloudsearch.Config{}, err
	}

	index, err := bleve.NewIndex(env.StoragePath, "")
	if err != nil {
		return cloudsearch.Config{}, err
	}

	results := bleve.NewResultStorage(index, meta, favorites)

	authService := auth.NewAuthenticator(
		authgateway.NewAuthGatewayClient(
			auth.DefaultGatewayUrl,
			env.HttpPort,
			&http.Client{
				Timeout: time.Second * 10,
			},
		),
	)

	registry := cloudsearch.NewRegistry() // nothing gets registered by default
	registerAll(registry, authService, accounts, env, results, enableCaching)

	thumbnails := cloudsearch.ThumbnailHandler{
		Meta: meta,
		Client: &http.Client{
			Timeout: time.Second * 10,
		},
	}

	multiSearch := cloudsearch.NewMultiSearch(
		env,
		accounts,
		results,
		registry,
		func(q cloudsearch.Query) []cloudsearch.ResultFilter {
			rf := []cloudsearch.ResultFilter{
				cloudsearch.SetId, // no better place to set this ugh
				cloudsearch.FilterNotInRange,
				cloudsearch.Dedup(q),
				cloudsearch.FilterContent,
			}

			if enableCaching {
				rf = append(rf,
					cloudsearch.FetchThumbsFilter(thumbnails),
				)
			}

			return rf
		},
	)

	indexer := cloudsearch.NewResultIndexer(
		accounts,
		results,
		favorites,
		meta,
		registry.FetchableBuilder,
	)

	// TODO move this side effect somewhere else
	go multiSearch.WatchTokens()

	return cloudsearch.Config{
		Env:              env,
		AccountsStorage:  accounts,
		SearchEngine:     &multiSearch,
		Registry:         registry,
		ResultsStorage:   results,
		FavoritesStorage: favorites,
		AuthService:      authService,
		MetadataStorage:  meta,
		ThumbnailHandler: thumbnails,
		ResultIndexer:    indexer,
	}, nil
}
