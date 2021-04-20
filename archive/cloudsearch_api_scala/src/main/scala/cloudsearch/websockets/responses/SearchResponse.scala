package cloudsearch.websockets.responses

import cloudsearch.search.Result

case class SearchResponse(query: String, service: String, resultType: String, results: List[Result]) extends SearchResult {

}
