package cloudsearch.server.table

import cloudsearch.index.IndexingMetadata
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import kotlin.reflect.KClass

fun <T> tableFor(clazz: KClass<*>): T {
    return Class.forName("cloudsearch.server.table.${clazz.simpleName}s").newInstance() as T
}

abstract class StringIdTable<T>(name: String) : IdTable<String>(name) {
    override val id: Column<EntityID<String>> = varchar("id", 100).entityId()

    abstract fun parseRow(it: ResultRow): T
    abstract fun toFields(r: T, t: UpdateBuilder<*>): Unit
}

object IndexingMetadatas : StringIdTable<IndexingMetadata>("IndexingMetadata") {
    val watermark = varchar("watermark", 100).nullable()
    val lastIndexedAt = datetime("indexed_at").nullable()

    override fun parseRow(it: ResultRow): IndexingMetadata {
        return IndexingMetadata(
                id = it[id].value,
                watermark = it[watermark],
                lastIndexedAt = it[lastIndexedAt]
        )
    }

    override fun toFields(r: IndexingMetadata, t: UpdateBuilder<*>) {
        t[watermark] = r.watermark
        t[lastIndexedAt] = r.lastIndexedAt
    }

}
