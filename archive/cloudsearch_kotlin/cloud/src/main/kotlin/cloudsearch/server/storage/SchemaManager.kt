package cloudsearch.server.storage

import cloudsearch.server.table.AccountConfigs
import cloudsearch.server.table.IndexingMetadatas
import cloudsearch.server.table.Results
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns
import org.jetbrains.exposed.sql.transactions.transaction

class SchemaManager(val config: DatabaseConfig) {

    fun migrate() {
        // TODO use connection pool
        Database.connect(config.url, config.driver)

        transaction {
            createMissingTablesAndColumns(
                    AccountConfigs(),
                    Results(),
                    IndexingMetadatas
            )
        }
    }

}