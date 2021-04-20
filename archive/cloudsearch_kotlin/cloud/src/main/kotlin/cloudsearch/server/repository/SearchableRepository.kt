package cloudsearch.server.repository

import cloudsearch.cache.ResultsCache
import cloudsearch.content.Result
import cloudsearch.search.Query
import cloudsearch.server.table.Results
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.coroutines.CoroutineContext

class SearchableRepository(
        val table: Results
) : ResultsCache {

    override fun clear() {
        table.deleteAll()
    }

    override fun get(i: String): Result? {
        return table.select {
            table.id.eq(i)
        }.singleOrNull()?.let { table.parseRow(it) }
    }

    override fun saveOrUpdate(res: Result): Boolean {
        return transaction {
            val updated = table.update(
                    where = {
                        table.id eq res.id
                    },
                    body = {
                        table.toFields(res, it)
                    }
            )

            // TODO wtf
            if (updated == 0) {
                val i = table.insert(
                        body = {
                            table.toFields(res, it)
                        }
                )
                i.execute(this)?.let { it > 0 }
            }
            updated > 0
        }
    }

    override fun delete(res: Result): Boolean {
        return deleteById(res.id)
    }

    override fun getAll(): List<Result> {
        return table.selectAll().map { table.parseRow(it) }
    }

    override fun deleteById(idd: String): Boolean {
        return table.deleteWhere {
            table.id.eq(idd)
        } > 0
    }

    override fun deleteAllById(keys: List<String>) {
        table.deleteWhere {
            table.id.inList(keys)
        } > 0
    }

    override fun count(): Int {
        return table.select {
            table.id.isNotNull()
        }.count()
    }

    suspend override fun search(query: Query, scope: CoroutineScope): ReceiveChannel<Result> = scope.produce {
        table.search(query).forEach {
            send(it)
        }
    }

}
