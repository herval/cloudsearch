package cloudsearch.cache

import cloudsearch.content.Result
import cloudsearch.search.Searchable
import cloudsearch.storage.Storage

interface ResultsCache : Searchable, Storage<Result>
