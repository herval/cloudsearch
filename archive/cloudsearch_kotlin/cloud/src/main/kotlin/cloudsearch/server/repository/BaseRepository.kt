package cloudsearch.server.repository

import cloudsearch.server.table.StringIdTable
import cloudsearch.storage.Identifiable
import cloudsearch.storage.Storage
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

open class BaseRepository<T : Identifiable>(val table: StringIdTable<T>) : Storage<T> {

    override fun clear() {
        table.deleteAll()
    }

    override fun get(i: String): T? {
        return table.select {
            table.id.eq(i)
        }.singleOrNull()?.let { table.parseRow(it) as T }
    }

    override fun saveOrUpdate(res: T): Boolean {
        return transaction {
            val updated = table.update(
                    where = {
                        table.id eq res.id
                    },
                    body = {
                        table.toFields(res as T, it)
                    }
            )

            // TODO wtf
            if (updated == 0) {
                val i = table.insert(
                        body = {
                            table.toFields(res as T, it)
                        }
                )
                i.execute(this)?.let { it > 0 }
            }
            updated > 0
        }
    }

    override fun delete(res: T): Boolean {
        return deleteById(res.id)
    }

    override fun getAll(): List<T> {
        return table.selectAll().map { table.parseRow(it) as T }
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

}