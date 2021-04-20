package cloudsearch.server.table

import cloudsearch.account.AccountType
import cloudsearch.content.ContentType
import cloudsearch.content.Result
import cloudsearch.search.InvolvementType
import cloudsearch.search.Query
import cloudsearch.search.SearchOrder
import cloudsearch.util.Parsers
import com.google.gson.Gson
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.joda.time.DateTime

class Results(
        val parser: Gson = Parsers.camelCased
) : StringIdTable<Result>("Results") {
    val fullText = text("fullText")
    val type = varchar("type", 255)
    val title = varchar("title", 255)
    val originalId = varchar("originalId", 255)
    val accountId = varchar("accountId", 255)
    val accountType = varchar("accountType", 255)
    val body = text("body").nullable()
    val cachedAt = datetime("cachedAt")
    val permalink = varchar("permalink", 255).nullable()
    val thumbnail = varchar("thumbnail", 255).nullable()
    val timestamp = datetime("timestamp")
    val involvesMe = bool("involves_me")
    val favorited = bool("favorited")
    val labels = varchar("labels", 255).nullable()

    val path = varchar("path", 255).nullable()
    val sizeBytes = long("size_bytes").nullable()

    // for lists of stuff. A JSON object.
    val details = text("details")

    fun search(query: Query): List<Result> {
        return this.select {
            val exprs = mutableListOf(
                    fullText.like("*${query.text}*")
            )

            if (query.after != null) {
                exprs.add(
                        timestamp.greater(query.after)
                )
            }
            if (query.before != null) {
                exprs.add(
                        timestamp.less(query.before)
                )
            }

            if (query.involving.contains(InvolvementType.Anyone)) {
                exprs.add(
                        involvesMe.eq(false)
                )
            } else {
                exprs.add(
                        involvesMe.eq(true)
                )
            }

            // TODO sort

            exprs.reduce { a, b -> a and b }
        }.orderBy(
                        timestamp,
                        isAsc = if (query.orderBy == SearchOrder.Ascending) {
                            true
                        } else {
                            false
                        })
                .map {
                    parseRow(it)
                }
    }

    override fun parseRow(it: ResultRow): Result {
        return Result(
                title = it[title],
                originalId = it[originalId],
                accountId = it[accountId],
                accountType = AccountType.valueOf(it[accountType]),
                body = it[body],
                cachedAt = it[cachedAt].millis,
                permalink = it[permalink],
                thumbnail = it[thumbnail],
                timestamp = it[timestamp].millis,
                labels = it[labels]?.split(",") ?: emptyList(),
                favorited = it[favorited],
                involvesMe = it[involvesMe],
                fullText = it[fullText],
                id = it[id].value,
                details = parser.fromJson<Map<String, Any?>>(it[details], Map::class.java),
                type = ContentType.valueOf(it[type])
        )
    }

    override fun toFields(r: Result, t: UpdateBuilder<*>) {
        t[id] = EntityID(r.id, this)
        t[title] = r.title
        t[originalId] = r.originalId
        t[accountId] = r.accountId
        t[accountType] = r.accountType.name
        t[body] = r.body
        t[cachedAt] = DateTime(r.cachedAt)
        t[permalink] = r.permalink
        t[thumbnail] = r.thumbnail
        t[timestamp] = DateTime(r.timestamp)
        t[labels] = r.labels.joinToString(",")
        t[favorited] = r.favorited
        t[involvesMe] = r.involvesMe
        t[fullText] = r.fullText
        t[details] = parser.toJson(r.details)
    }
}
