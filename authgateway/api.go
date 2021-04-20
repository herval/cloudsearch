package authgateway

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"golang.org/x/oauth2"
	"strings"
	"time"
)

type Api struct {
	BaseUrl string
}

type Token struct {
	AccessToken  string    `json:"access_token"`
	TokenType    string    `json:"token_type"`
	RefreshToken string    `json:"refresh_token"`
	Expiry       time.Time `json:"expiry"`
}

func (a *Api) StartServer(port string, env Config) error {
	s := gin.Default()

	s.GET(AuthorizePath(""), oauthAuthUrl(env))
	s.GET(TokenPath(""), oauthTokenExchange(env))
	s.GET(RefreshTokenPath(""), oauthRefreshToken(env))

	fmt.Println("Starting server on port " + port)
	return s.Run(port)
}

func RefreshTokenPath(service string) string {
	res := "/oauth2/refresh_token/"
	if service == "" {
		return res + ":serviceName"
	}
	return res + service
}

func TokenPath(service string) string {
	res := "/oauth2/token_for_code/"
	if service == "" {
		return res + ":serviceName"
	}
	return res + service
}

func AuthorizePath(service string) string {
	res := "/oauth2/authorize_url/"
	if service == "" {
		return res + ":serviceName"
	}
	return res + service
}

// called by your service. The response will be a redirect to the OAuth2 authorize url, or included in the
// response body if a "format" param is supplied
func oauthAuthUrl(env Config) func(ctx *gin.Context) {
	return func(ctx *gin.Context) {
		service := ctx.Param("serviceName")
		scopes := ctx.Query("scopes")
		format := ctx.Query("format")
		redirect := ctx.Query("redirectUrl")

		creds := env.ProviderFor(service)
		if creds == nil {
			ctx.Status(404)
			return
		}
		c := *creds

		if redirect == "" {
			ctx.Status(400)
			return
		}

		sc := []string{}
		if scopes != "" { // override scopes
			sc = strings.Split(scopes, ",")

		}

		url := c.AuthUrlFor(env, redirect, sc)

		switch format {
		case "plain":
			ctx.Data(200, "text/plain", []byte(url))
		default:
			ctx.Redirect(302, url)
		}
	}
}

// called to finalize the token exchange process.
func oauthTokenExchange(env Config) func(ctx *gin.Context) {
	return func(ctx *gin.Context) {
		service := ctx.Param("serviceName")
		code := ctx.Query("code")
		redirect := ctx.Query("redirectUrl")

		creds := env.ProviderFor(service)
		if creds == nil {
			ctx.Status(404)
			return
		}

		token, err := creds.TokenFromCode(ctx.Request.Context(), code, redirect, creds.Scopes)
		if err != nil {
			ctx.JSON(406,
				gin.H{
					"error": err.Error(),
				},
			)
			return
		}

		ctx.JSON(200, token)
	}
}

// called when you need to refresh an expired token.
func oauthRefreshToken(env Config) func(ctx *gin.Context) {
	return func(ctx *gin.Context) {
		service := ctx.Param("serviceName")
		t := ctx.Query("accessToken")
		tt := ctx.Query("tokenType")
		r := ctx.Query("refreshToken")
		redirect := ctx.Query("redirectUrl")

		ot := &oauth2.Token{
			AccessToken:  t,
			TokenType:    tt,
			RefreshToken: r,
		}

		creds := env.ProviderFor(service)
		if creds == nil {
			ctx.Status(404)
			return
		}

		tok, err := creds.RefreshToken(ctx, ot, redirect, creds.Scopes)
		if err != nil {
			ctx.JSON(406,
				gin.H{
					"error": err.Error(),
				},
			)
			return
		}

		ctx.JSON(200, tok)
	}
}
