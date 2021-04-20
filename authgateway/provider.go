package authgateway

import (
	"context"
	"golang.org/x/oauth2"
)

type Provider struct {
	Name           string            `yaml:"name"`
	ClientId       string            `yaml:"clientId"`
	ClientSecret   string            `yaml:"clientSecret"`
	AuthUrl        string            `yaml:"authUrl"`
	TokenUrl       string            `yaml:"tokenUrl"`
	AuthCodeParams map[string]string `yaml:"authCodeParams"`
	Scopes         []string          `yaml:"scopes"`

	//RedirectUrl     string                  `yaml:"-"`
	config          oauth2.Config           `yaml:"-"`
	authCodeOptions []oauth2.AuthCodeOption `yaml:"-"`
}

// validate the provider and build the config attribute (mutable state ew)
func (p *Provider) Parse(c Config) error {
	p.authCodeOptions = []oauth2.AuthCodeOption{}
	for k, v := range p.AuthCodeParams {
		p.authCodeOptions = append(
			p.authCodeOptions,
			oauth2.SetAuthURLParam(k, v),
		)
	}

	return nil
}

func oauth(p Provider, redirectUrl string, scopes []string) *oauth2.Config {
	sc := p.Scopes
	if scopes != nil && len(scopes) > 0 {
		sc = scopes
	}

	return &oauth2.Config{
		ClientID:     p.ClientId,
		ClientSecret: p.ClientSecret,
		RedirectURL:  redirectUrl,
		Endpoint: oauth2.Endpoint{
			AuthURL:  p.AuthUrl,
			TokenURL: p.TokenUrl,
		},
		Scopes: sc,
	}
}

func (p Provider) AuthUrlFor(c Config, redirectUrl string, scopes []string) string {
	return oauth(p, redirectUrl, scopes).AuthCodeURL(c.Secret, p.authCodeOptions...)
}

func (p Provider) TokenFromCode(ctx context.Context, code string, redirectUrl string, scopes []string) (*Token, error) {
	return tok(oauth(p, redirectUrl, scopes).Exchange(ctx, code))
}

func (p Provider) RefreshToken(ctx context.Context, token *oauth2.Token, redirectUrl string, scopes []string) (*Token, error) {
	return tok(oauth(p, redirectUrl, scopes).TokenSource(ctx, token).Token())
}

func tok(token *oauth2.Token, err error) (*Token, error) {
	if err != nil {
		return nil, err
	}

	if token != nil {
		return &Token{
			AccessToken:  token.AccessToken,
			RefreshToken: token.RefreshToken,
			Expiry:       token.Expiry,
			TokenType:    token.TokenType,
		}, nil
	}
	return nil, nil
}
