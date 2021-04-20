package authgateway

import (
	"encoding/json"
	"fmt"
	"github.com/herval/authgateway"
	"github.com/pkg/errors"
	"io/ioutil"
	"net/http"
	"net/url"
	"strings"
	"time"
)

// Start a local auth server, tied to a remote authgateway.
// This side is needed so the "code" callback can be properly routed to the local application,
// and from it to the remote gateway, thus not exposing or embedding client ids/secrets on the client-side
func NewAuthGatewayClient(
	authGatewayServerUrl string,
	localPort string,
	client *http.Client,
) AuthClient {
	return AuthClient{
		authGatewayServerUrl,
		client,
	}
}

type AuthClient struct {
	authGatewayServerUrl string
	client               *http.Client
}

func (a *AuthClient) AuthorizeUrl(accountType string, redirectUrl string) (string, error) {
	res, err := a.get(
		"/oauth2/authorize_url/"+strings.ToLower(accountType),
		map[string]string{
			"redirectUrl": redirectUrl,
			"format":      "plain",
		},
	)

	if err != nil {
		return "", errors.Wrap(err, "Could not get an auth url")
	}

	data, err := ioutil.ReadAll(res.Body)
	if err != nil {
		return "", errors.Wrap(err, "Could not read auth body!")
	}

	return string(data), nil
}

func (a *AuthClient) TokenFromCode(accountType string, code string, redirectUrl string) (*authgateway.Token, error) {
	return a.getAndParseToken(
		"/oauth2/token_for_code/"+strings.ToLower(accountType),
		accountType,
		map[string]string{
			"code":        code,
			"redirectUrl": redirectUrl,
		},
	)
}

func (a *AuthClient) RefreshToken(accountType string, token authgateway.Token, redirectUrl string) (authgateway.Token, bool, error) {
	if token.Expiry.After(time.Now().Add(time.Minute * 20)) {
		// no need to update if not expired
		return token, false, nil
	}

	tok, err := a.getAndParseToken(
		"/oauth2/refresh_token/"+strings.ToLower(accountType),
		accountType,
		map[string]string{
			"accessToken":  token.AccessToken,
			"refreshToken": token.RefreshToken,
			"tokenType":    token.TokenType,
			"redirectUrl":  redirectUrl,
		},
	)
	if err != nil {
		return token, false, err
	}

	return *tok, true, err
}

func (a *AuthClient) get(path string, params map[string]string) (*http.Response, error) {
	va := url.Values{}
	for k, v := range params {
		va.Add(k, v)
	}

	res, err := a.client.Get(
		fmt.Sprintf("%s%s?%s",
			a.authGatewayServerUrl,
			path,
			va.Encode(),
		),
	)

	return res, err
}

func (a *AuthClient) getAndParseToken(path string, accountType string, params map[string]string) (*authgateway.Token, error) {
	res, err := a.get(path, params)

	if err != nil {
		return nil, err
	}

	var token authgateway.Token

	data, err := ioutil.ReadAll(res.Body)
	if err != nil {
		return nil, err
	}

	err = json.Unmarshal(data, &token)
	if err != nil {
		return nil, errors.Wrap(err, "could not unmarshal token")
	}

	return &token, err
}
