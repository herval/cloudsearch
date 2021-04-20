package api

import (
	"context"
	"fmt"
	"github.com/gin-gonic/gin"
	cloudsearch "github.com/herval/cloudsearch-desktop-agent/pkg"
	"github.com/herval/cloudsearch-desktop-agent/pkg/assets"
	"github.com/sirupsen/logrus"
	"gopkg.in/olahol/melody.v1"
	"os"
	"time"
)

// TODO use cloudsearch.Config
type Api struct {
	Config              cloudsearch.Config
	CurrentSearchCancel context.CancelFunc
}

func OauthRedirectUrlFor(env cloudsearch.Env, accountType cloudsearch.AccountType) string {
	return fmt.Sprintf("%s%s/accounts/oauth/callback/%s",
		env.ServerBase,
		env.HttpPort,
		accountType,
	)
}

func (a *Api) Start(port string, debug bool) error {
	if !debug {
		gin.SetMode(gin.ReleaseMode)
	}
	s := gin.Default()

	m := melody.New()

	s.GET("/accounts/oauth/callback/:service", a.oauthCallback)
	s.GET("/accounts/auth_url", a.authUrl)
	s.GET("/accounts/list", a.listAccounts)
	s.GET("/search", a.search)
	s.GET("/accounts/remove/:id", a.removeAccount)
	s.GET("/contents/favorite/:content_id", a.toggleFavorite)
	s.GET("/contents/favorites", a.listFavorites)
	s.GET("/contents/thumbnail/:account_id/:content_id", a.thumbnail)
	s.GET("/searches/saved", a.savedSearches)
	s.GET("/searches/recent", a.recentSearches)
	s.GET("/kill", func(i *gin.Context) {
		os.Exit(0)
	})

	//s.GET("/accounts/create", a.createAccount) // TODO is this used?

	s.GET("/accounts/basic_auth/create")
	s.GET("/health", a.health)

	s.GET("/socket/search", a.socketSearch(m))
	m.HandleMessage(a.searchSocket)

	logrus.Info("Server starting on ", port)
	return s.Run(port)
}

func (a *Api) health(ctx *gin.Context) {
	ctx.JSON(200,
		map[string]interface{}{
			"pid": os.Getpid(),
		})
}

func (a *Api) socketSearch(m *melody.Melody) func(ctx *gin.Context) {
	return func(ctx *gin.Context) {
		err := m.HandleRequest(ctx.Writer, ctx.Request)
		if err != nil {
			logrus.Error("Cannot handle websocket: ", err)
		}
	}
}

//return Promise.resolve([
//{title: 'type:contact', previewable: false, kind: 'Search'},
//{title: 'type:document', previewable: false, kind: 'Search'},
//{title: 'type:calendar', previewable: false, kind: 'Search'},
//{title: 'type:email service:google after:2017-01-01', previewable: false, kind: 'Search'},
//])
func (a *Api) recentSearches(ctx *gin.Context) {
	res := []interface{}{}

	ctx.JSON(200, map[string]interface{}{
		"Results": res,
	})
}

func (a *Api) thumbnail(ctx *gin.Context) {
	id := ctx.Param("content_id")
	c, err := a.Config.ResultsStorage.Get(id)

	if err != nil {
		renderError(ctx, err)
		return
	}

	if c == nil || c.Thumbnail == "" {
		ctx.Status(404)
		return
	}

	img, contentType, err := a.Config.ThumbnailHandler.GetThumbnail(*c)
	if err != nil {
		logrus.Debug("No thumbnail cached for ", c.Id, ": ", err)
	} else {
		ctx.Data(
			200,
			contentType,
			img,
		)
		return
	}

	logrus.Debug("redirecting to original thumbnail: ", c.Thumbnail)
	ctx.Redirect(302, c.Thumbnail)
}

func (a *Api) savedSearches(ctx *gin.Context) {
	placeholders := []cloudsearch.Result{
		{
			Title:       "type:Contact",
			ContentType: cloudsearch.SearchQuery,
		},
		{
			Title:       "type:Event after:" + cloudsearch.QueryFormattedTime(time.Now()),
			ContentType: cloudsearch.SearchQuery,
		},
	}

	res := []interface{}{}
	for _, r := range placeholders {
		res = append(res, cloudsearch.ResultJson(
			&r,
			cloudsearch.Query{},
			cloudsearch.SavedSearches,
			a.Config.Env,
		))
	}

	ctx.JSON(200, map[string]interface{}{
		"Results": res,
	})
}

func (a *Api) listFavorites(ctx *gin.Context) {
	faves, err := a.Config.ResultsStorage.AllFavorited()
	if err != nil {
		renderError(ctx, err)
		return
	}

	fav := []interface{}{}
	for _, f := range faves {
		fav = append(fav, cloudsearch.ResultJson(
			&f,
			cloudsearch.Query{},
			cloudsearch.Favorites,
			a.Config.Env,
		))
	}

	ctx.JSON(200, map[string]interface{}{
		"Results": fav,
	})
}

func (a *Api) toggleFavorite(ctx *gin.Context) {
	id := ctx.Param("content_id")
	favorited, err := a.Config.FavoritesStorage.ToggleFavorite(id)
	if err != nil {
		renderError(ctx, err)
		return
	}

	ctx.JSON(200, map[string]interface{}{
		"Results": favorited,
	})
}

func (a *Api) removeAccount(ctx *gin.Context) {
	id := ctx.Param("id")

	err := a.Config.SearchEngine.DeleteAccount(id)
	if err != nil {
		renderError(ctx, err)
		return
	}

	ctx.JSON(200, map[string]interface{}{
		"deleted": true,
	})
}

//func (a *Api) createAccount(ctx *gin.Context) {
//	service := ctx.Query("service")
//	token := ctx.Query("token")
//
//	s.authBuilder.ForService(account.AccountType)
//}

func (a *Api) authUrl(ctx *gin.Context) {
	service, err := a.Config.Registry.ParseAccountType(ctx.Query("service"))
	if err != nil {
		renderError(ctx, err)
		return
	}

	acc := cloudsearch.AccountType(service)
	url, err := a.Config.AuthService.AuthorizeUrl(
		acc,
		OauthRedirectUrlFor(a.Config.Env, acc),
	)
	if err != nil {
		renderError(ctx, err)
		return
	}

	ctx.JSON(
		200,
		map[string]interface{}{
			"url": url,
		},
	)
}

func (a *Api) oauthCallback(ctx *gin.Context) {
	service, err := a.Config.Registry.ParseAccountType(ctx.Param("service"))
	code := ctx.Query("code")
	logrus.Debug("Oauth callback for ", service, " - ", code)

	auth, err := a.Config.Registry.AuthBuilder(service)
	if err != nil {
		renderError(ctx, err)
		return
	}

	aa := cloudsearch.AccountType(service)
	acc, err := a.Config.AuthService.AccountFromCode(
		aa,
		code,
		OauthRedirectUrlFor(a.Config.Env, aa),
	)
	if err != nil || acc == nil {
		renderError(ctx, err)
		return
	}

	acc, err = auth.FetchIdentityInfo(*acc)
	if err != nil {
		renderError(ctx, err)
		return
	}

	err = a.Config.SearchEngine.SaveAccount(acc)
	if err != nil {
		renderError(ctx, err)
		return
	}

	logrus.Debug("Account saved: ", acc)

	data, err := assets.Static("account_linked.html")
	if err != nil {
		logrus.Debug("Data missing: ", err)
		ctx.Status(500)
	}

	ctx.Data(200, "text/html", data)
}

func renderError(context *gin.Context, err error) {
	logrus.Error("Rendering error: ", err)
	context.JSON(
		406,
		map[string]interface{}{
			"error": err.Error(),
		},
	)
}

func (a *Api) search(ctx *gin.Context) {
	q := ctx.Query("query")
	qq := cloudsearch.ParseQuery(q, cloudsearch.NewId(), a.Config.Registry)
	res := a.Config.SearchEngine.Search(qq, ctx.Request.Context())

	ret := []map[string]interface{}{}

	for q := range res {
		if q.Status == cloudsearch.ResultFound {
			ret = append(ret, cloudsearch.ResultJson(
				&q,
				qq,
				cloudsearch.RegularSearch,
				a.Config.Env,
			))
		} else {
			logrus.Warn("Unwanted result: ", q)
		}
	}

	// TODO err?

	ctx.JSON(200, map[string]interface{}{
		"Results": ret,
	})
}

func (a *Api) listAccounts(ctx *gin.Context) {
	acc, err := a.Config.SearchEngine.AllAccounts()

	res := []map[string]interface{}{}
	for _, a := range acc {
		res = append(res, a.JsonFields())
	}

	if err != nil {
		logrus.Error("Listing", err)
		ctx.Status(500)
	} else {
		ctx.JSON(200, res)
	}
}
