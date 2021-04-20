## TODO

- Compile to native w/ graal
- Indexer to keep a list of recently changed stuff (recent emails, upcoming meetings, etc)
- Cache image thumbs
- Include people associated w/ each document
- Tags?

### Commercial license?

See all the activity happening in one place beyond the last 30 days limit.
Connect all the apps you and your team use, without any limits.

## Running locally

gradle assembleDist

gradle run

swagger-codegen generate -i http://petstore.swagger.io/v2/swagger.json -l ruby -o /tmp/test/

https://github.com/electron/electron/blob/master/docs/tutorial/mac-app-store-submission-guide.md

eg websockets
{ "op": "get_auth_url", "service": "google"}

{ "op": "new_account", "service": "dropbox", "token": "ofrAferCfS4AAAAAAAvwLsmN8-plyKoIMoA3PQoGLIw"}

{ "op": "search", "query": "google"}

TO DO

- EXPIRE ALL KEYS ON .env!

https://github.com/settings/applications/642885

https://www.linkedin.com/developer/apps/3369281/auth

https://myaccount.google.com/permissions

curl "http://0.0.0.0:65432/search?query=foo"
curl "http://0.0.0.0:65432/search?query=foo&contents=Task"
curl "http://0.0.0.0:65432/accounts/list"
curl "http://0.0.0.0:65432/accounts/auth_url?service=Google"
curl "http://0.0.0.0:65432/accounts/create?service=Google&token=xxxxx"
curl "http://0.0.0.0:65432/accounts/basic_auth/create?service=Jira&username=jiratest&password=jiratest123&server=issues.apache.org/jira"

https://www.dropbox.com/developers/apps/info/33423aj9h4o1hmf

https://console.developers.google.com/apis/dashboard?project=api-project-404216507544&duration=P2D
