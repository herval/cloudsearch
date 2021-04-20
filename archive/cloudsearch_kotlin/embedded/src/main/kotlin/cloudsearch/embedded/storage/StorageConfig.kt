package cloudsearch.embedded.storage

import cloudsearch.config.Config


data class StorageConfig(val storagePath: String) {
    companion object {
        fun fromConfig(config: Config) = StorageConfig(
                config.get("STORAGE_PATH")!!
        )
    }
}