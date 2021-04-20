package cloudsearch.server.storage

import cloudsearch.config.Config

data class DatabaseConfig(
        val driver: String,
        val url: String
) {

    companion object {

        fun fromConfig(config: Config): DatabaseConfig {
            return DatabaseConfig(
                    config.get("DATABASE_DRIVER")!!,
                    config.get("DATABASE_URL")!!
            )
        }

    }


}