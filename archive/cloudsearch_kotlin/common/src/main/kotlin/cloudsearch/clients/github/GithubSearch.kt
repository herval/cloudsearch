package cloudsearch.clients.github

import cloudsearch.clients.http.ClientBuilder
import cloudsearch.clients.http.OauthToken
import cloudsearch.content.ContentType
import cloudsearch.search.Query
import cloudsearch.search.SearchOrder
import cloudsearch.search.SimpleSearchable
import cloudsearch.storage.AccountConfig
import cloudsearch.util.Parsers
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass


// https://help.github.com/articles/understanding-the-search-syntax/
// github has quite a few searches...
abstract class GithubSearch<J : Any>(
        jsonKind: KClass<J>,
        path: String,
        account: AccountConfig,
        contentKinds: List<ContentType>
) : SimpleSearchable<J>(
        client = ClientBuilder.build(
                baseUrl = "https://api.github.com",
                kind = jsonKind,
                jsonParser = Parsers.underscored,
                credentials = OauthToken(account.credentials)
        ),
        path = path,
        postEncoding = null,
        contentKinds = contentKinds
) {
    private val log = LoggerFactory.getLogger(javaClass.name)

    override fun params(query: Query): Map<String, String> {
        // TODO after/before
        return mapOf(
                "order" to if (query.orderBy == SearchOrder.Ascending) {
                    "asc"
                } else {
                    "desc"
                }
        )
    }

}