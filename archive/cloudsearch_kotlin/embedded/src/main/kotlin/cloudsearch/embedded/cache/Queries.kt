package cloudsearch.embedded.cache

import cloudsearch.account.AccountType
import cloudsearch.content.ContentType
import cloudsearch.content.Result
import cloudsearch.search.InvolvementType
import cloudsearch.search.ResultStatus
import cloudsearch.search.SearchOrder
import cloudsearch.search.Tokenizer
import com.googlecode.cqengine.attribute.support.SimpleFunction
import com.googlecode.cqengine.query.Query
import com.googlecode.cqengine.query.QueryFactory
import com.googlecode.cqengine.query.QueryFactory.attribute
import com.googlecode.cqengine.query.logical.And
import com.googlecode.cqengine.query.logical.Or
import com.googlecode.cqengine.query.option.AttributeOrder
import com.googlecode.cqengine.query.option.OrderByOption
import com.googlecode.cqengine.query.option.QueryOptions
import com.googlecode.cqengine.query.simple.*

data class QueryData(
        val query: Query<Result>,
        val options: QueryOptions
)

interface SearchQuery {
    fun query(query: cloudsearch.search.Query): QueryData?
}

open class BasicQuery : SearchQuery {
    private val clazz = Result::class

    val favorited = attribute(clazz.java, Boolean::class.java, "favorited", SimpleFunction { t -> t.favorited })
    val accountId = attribute(clazz.java, String::class.java, "accountId", SimpleFunction { t -> t.accountId })
    val fullText = attribute(clazz.java, String::class.java, "fullText", SimpleFunction { t -> t.fullText })
    val involvingMe = attribute(clazz.java, Boolean::class.java, "involvesMe", SimpleFunction { t -> t.involvesMe })
    val contentType = attribute(clazz.java, ContentType::class.java, "kind", SimpleFunction { t -> t.type })
    val timestamp = attribute(clazz.java, Long::class.java, "timestamp", SimpleFunction { t -> t.timestamp })
    val accountType = attribute(clazz.java, AccountType::class.java, "accountType", SimpleFunction { t -> t.accountType })

    override fun query(query: cloudsearch.search.Query): QueryData? {

        val subqueries = mutableListOf<Query<Result>?>(
                queryFullText(query)
        )

        val accounts = query.accounts
        if (accounts != null) {
            In(accountId, false, accounts.map { it.id }.toSet())
        }

        val statuses = query.statuses
        if (statuses != null && statuses.contains(ResultStatus.Favorited)) {
            subqueries.add(
                    Equal(favorited, true)
            )
        }

        val accountTypes = query.accountTypes
        if (accountTypes  != null) {
            subqueries.add(
                    In(accountType, false, accountTypes.toSet())
            )
        }


        val types = query.contentTypes
        if (types != null) {
            subqueries.add(
                    In(contentType, false, types.toSet())
            )
        }

        if (query.involving.contains(InvolvementType.Anyone)) {
            subqueries.add(
                    Equal(involvingMe, false)
            )
        } else {
            subqueries.add(
                    Equal(involvingMe, true)
            )
        }

        val after = query.after?.millis
        if (after != null) {
            subqueries.add(
                    GreaterThan(timestamp, after, true)
            )
        }

        val before = query.before?.millis
        if (before != null) {
            subqueries.add(
                    LessThan(timestamp, before, true)
            )
        }

        // ordering
        val ordering = query.orderBy.let {
            when (it) {
                SearchOrder.Default -> null
                SearchOrder.Ascending ->
                    OrderByOption(listOf(
                            AttributeOrder(timestamp, false)
                    ))
                SearchOrder.Descending ->
                    OrderByOption(listOf(
                            AttributeOrder(timestamp, true)
                    ))
            }
        }

        val q = and(
                subqueries.filterNotNull()
        )
        return if (q == null) {
            null
        } else {
            QueryData(
                    q,
                    QueryFactory.queryOptions(
                            listOf(
                                    ordering
                            ).filterNotNull()
                    )
            )
        }
    }

    private fun queryFullText(query: cloudsearch.search.Query): Query<Result>? {
        val t = Tokenizer.tokens(query.text.toLowerCase())
        val q: List<Query<Result>> = t.map {
            StringContains(fullText, it)
        }

        return and(q)
    }

    private fun and(q: List<Query<Result>>?): Query<Result>? {
        return if (q == null || q.isEmpty()) {
            null
        } else if (q.size > 1) {
            And(q)
        } else {
            q[0]
        }
    }

    private fun or(q: List<Query<Result>>): Query<Result> {
        return if (q.size > 1) {
            Or(q)
        } else {
            q[0]
        }
    }
}