# CloudSearch Plus: a tiny realtime search tool for cloud accounts [closed source]

Open source on github.com/herval/cloudsearch

## Usage

> cloudsearch login <account type>
> cloudsearch search foo before:2006-02-01 after: 2005-02-01 mode:cache type:Email type:Image service:Google
> cloudsearch accounts list
> cloudsearch accounts remove <account id>

# TO DO

- Fix result order (newer should be first?)
- Fix search w/ lowercase on titles (not working? eg email subject)
- Network listener
- Thumbnails
- Cache queries to only hit downstream after 1 minute if they successfully returned
- Fix empty query search on cache w/ type predicates
- "Basic" account setup
- Context around search result
- Fix result order (newer should be first?)
- Confluence
- Jira
- Cache queries to only hit downstream after 1 minute if they successfully returned
- Fix empty query search on cache w/ type predicates

brew install leptonica
brew install tesseract
export CGO_LDFLAGS="-L$(brew --prefix leptonica)/lib -L$(brew --prefix tesseract)/lib"
export CGO_CFLAGS="-I$(brew --prefix leptonica)/include -I$(brew --prefix tesseract)/include"

## Sample commands

go run ./cmd search foo

go run ./cmd --log --debug interactive

go run ./cmd --index server

go run ./cmd login Google

## APIs

curl "http://127.0.0.1:65432/search?query=foo"
curl "http://127.0.0.1:65432/search?query=foo&contents=Task"
curl "http://127.0.0.1:65432/accounts/list"
curl "http://127.0.0.1:65432/accounts/auth_url?service=Dropbox"
curl "http://127.0.0.1:65432/accounts/auth_url?service=Google"
curl "http://127.0.0.1:65432/accounts/create?service=Google&token=4/7IDPAJfsk3PQwo8NflbwMnRB5RnqY8huFO2TijbDS74"
curl "http://127.0.0.1:65432/accounts/basic_auth/create?service=Jira&username=jiratest&password=jiratest123&server=issues.apache.org/jira"

https://download.qt.io/official_releases/online_installers/qt-unified-mac-x64-online.dmg
install the experimental webkit module: https://github.com/annulen/webkit/releases/download/qtwebkit-5.212.0-alpha2/qtwebkit-5.212.0_alpha2-qt59-darwin-x64.tar.xz, extract it's content inside $HOME\Qt\5.11.1\clang_64 (or similar)
QT_WEBKIT=true
export QT_DIR=/Users/herval/Qt/

go get -v github.com/therecipe/qt/cmd/...
$GOPATH/bin/qtsetup

compile flags:
CGO_LDFLAGS_ALLOW='-Wl,-rpath,@executable_path/Frameworks'

## TODO

[ ] make parameters work again?!
