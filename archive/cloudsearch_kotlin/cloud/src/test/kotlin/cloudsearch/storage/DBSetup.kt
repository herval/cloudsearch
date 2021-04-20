package cloudsearch.storage

import cloudsearch.server.storage.SchemaManager
import cloudsearch.server.storage.DatabaseConfig
import junit.framework.TestCase

fun setupDb() {

    val conf = DatabaseConfig(
            url = "jdbc:h2:mem:test",
            driver = "org.h2.Driver"
    )

    val manager = SchemaManager(conf)

    manager.migrate()
}