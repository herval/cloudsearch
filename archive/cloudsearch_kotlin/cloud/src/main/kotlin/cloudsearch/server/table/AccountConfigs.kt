package cloudsearch.server.table

import cloudsearch.account.AccountType
import cloudsearch.storage.AccountConfig
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime


class AccountConfigs : StringIdTable<AccountConfig>("AccountConfigs") {
    val active = bool("active")
    val originalId = varchar("originalId", 255)
    val username = varchar("username", 255)
    val groupName= varchar("groupName", 255).nullable()
    val expiresAt = datetime("expires_at").nullable()
    val credentials= varchar("token", 255)
    val apiServer= varchar("apiServer", 255).nullable()
    val type = varchar("type", 255)
    val refreshCredentials = varchar("refreshCredentials", 255).nullable()
    val scopes = varchar("scopes", 255)

    fun all(): List<AccountConfig> {
        return selectAll().map { parseRow(it) }
    }

    fun get(i: String): AccountConfig? {
        return select {
            id.eq(i)
        }.singleOrNull()?.let { parseRow(it) }
    }

    fun delete(idd: String): Boolean {
        return deleteWhere {
            id.eq(idd)
        } != 0
    }


    fun saveOrUpdate(res: AccountConfig): Boolean {
        return transaction {
            val updated = update(
                    where = {
                        id eq res.id
                    },
                    body = {
                        toFields(res, it)
                    }
            )

            // TODO wtf
            if (updated == 0) {
                val i = insert(
                        body = {
                            toFields(res, it)
                        }
                )
                i.execute(this)?.let { it > 0 }
            }
            updated > 0
        }
    }

    override fun toFields(r: AccountConfig, t: UpdateBuilder<*>) {
        t[id] = EntityID(r.id, this)
        t[active] = r.active
        t[originalId] = r.originalId
        t[username] = r.username
        t[groupName] = r.groupName
        t[expiresAt] = r.expiresAt?.let { DateTime(it) }
        t[credentials] = r.credentials
        t[apiServer] = r.apiServer
        t[type] = r.type.name
        t[refreshCredentials] = r.refreshCredentials
        t[scopes] = r.scopes.joinToString(",")

    }

    override fun parseRow(it: ResultRow): AccountConfig {
        return AccountConfig(
                active = it[active],
                originalId = it[originalId],
                username = it[username],
                groupName = it[groupName],
                expiresAt = it[expiresAt]?.millis,
                credentials = it[credentials],
                apiServer = it[apiServer],
                type = AccountType.valueOf(it[type]),
                refreshCredentials = it[refreshCredentials],
                scopes = it[scopes].split(","),
                id = it[id].value
        )
    }
}
