# Auth Gateway

A simple Oauth2 gateway for apps that need to do OAuth2 authorization w/ multiple services, 
but don't want (or cannot) embedd client ids/secrets (eg for apps distributed as a command line tool).

This service doesn't store or log anything, and it's purposefully designed to be as simple as possible, both in terms
of configuration and deploy.

## WHY?

A lot of my projects revolve around building command line tools or small clients to do stuff such as communicating w/ 
Twitter or reading data from Google Drive. 

In order to properly distribute those, one needs to be able to roll out proper OAuth2 authorization. 
Embedding keys is a risk, as they can leak and be used for who-knows-what, and distributing them
on OSS is even worse - you're pretty much guaranteed to have your keys leaked in minutes. 

This little project encapsulates the OAuth2 dance + configs for multiple providers at once,
so one can simply edit a yml file, deploy it anywhere, and focus on building the actual app - no embedded keys needed. 

## Configuration

1. Configure your services in a YAML file - see services.yaml.example for examples
2. Run the server with `authgateway --config <path to your config file>`
3. Obtain a ClientId/ClientSecret pair
4. Configure `<your server>/oauth2/callback/:serviceName` as the callback url on OAuth service (replacing `:serviceName` with the identifier you used on the yaml config - eg `google` or `dropbox`) 

This project includes a Dockerfile, so all you need to do to have it running locally is make a `services.yaml` file based on the `services.yaml.example` file, then run `./build.sh` and `./run.sh`.

## Usage

There's a couple endpoints your app will call directly:

```
GET /oauth2/authorize_url/:serviceName
```

Redirect the browser to the authorization URL of your given service. You need to specify a *local* redirect_uri here,
which will be passed to the Oauth provider. Usually, all you need to do with this callback is post back the params to the
Auth Gateway, in order to exchange the code for a token.

Acceptable params:
`scopes` - a comma-separated list of strings
`redirect_uri` - a local URL to deal with the "code" response from oauth.


```
GET /oauth2/exchange_token/:serviceName
```

```
GET /oauth2/refresh_token/:serviceName
```

```
GET /oauth2/callback/:serviceName
```

Called by the OAuth provider to finalize the token exchange process