package authgateway_test

import (
	"github.com/herval/authgateway"
	"testing"
)

func TestParsing(t *testing.T) {
	env, err := authgateway.ParseConfig("./services.yaml.example")
	if err != nil {
		t.Fatal(err)
	}

	if env.ProviderFor("dropbox").ClientId != "yyyyy" ||
		env.ProviderFor("dropbox").ClientSecret != "yyyyy" {
		t.Fatal("Parsing failed: ", env)
	}

	if env.ProviderFor("google").AuthUrl != "https://accounts.google.com/o/oauth2/v2/auth" {
		t.Fatal("Parsing auth url failed: ", env)
	}

	if env.ProviderFor("google").AuthCodeParams["access_type"] != "offline" {
		t.Fatal("Parsing auth code params failed: ", env)
	}

	if s := env.ProviderFor("slack").Scopes; len(s) != 1 || s[0] != "search:read" {
		t.Fatal("Parsing scopes failed: ", env)
	}

	if env.Secret != "foobar" {
		t.Fatal("Parsing secret failed: ", env)
	}
}

func TestAuthUrl(t *testing.T) {
	env, err := authgateway.ParseConfig("./services.yaml.example")
	if err != nil {
		t.Fatal(err)
	}

	if u := env.ProviderFor("google").AuthUrlFor(*env, "localhost/foo", []string{"foo"}); u != "https://accounts.google.com/o/oauth2/v2/auth?access_type=offline&client_id=xxxxxx&redirect_uri=localhost%2Ffoo&response_type=code&scope=foo&state=foobar" {
		t.Fatal("Auth url failed: ", u)
	}
}
