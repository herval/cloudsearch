# Cloudsearch Multisearch API

This is a bit of the backend for a project that never got released. 

It exposes a websocket API that allows searching using multiple backend APIs in parallel (currently Gmail messages, 
Gmail contacts, Google Drive and Dropbox). Open-sourced for posterity/infamy. It reads account data (oauth tokens, etc) 
from a database (postgresql by default).


## Build & Run ##

```sh
sbt compile stage
source .env.example
./target/universal/stage/bin/cloudsearch-api
```

Or, using Docker:

```sh
docker build -t api .
docker run -p 8080:8080 --env-file=.env.example api 
```


## TODO

- Database schema
- Individual soft-timeouts for slow APIs (Gmail, etc)
- Non-websocket API
- Websocket demo client