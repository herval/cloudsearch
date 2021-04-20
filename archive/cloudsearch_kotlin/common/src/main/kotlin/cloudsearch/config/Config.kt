package cloudsearch.config

import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.util.*
import java.util.logging.Logger

/**
 * Created by hfreire on 1/8/17.
 */
class Config(envFile: String = ".env") {

    private val prop = Properties()
    private val logger = LoggerFactory.getLogger(javaClass.name)

    init {
        try {
            logger.info("Loading env from bundle")
            javaClass.classLoader.getResourceAsStream(envFile).use {
                prop.load(it)
            }
        } catch (e: Exception) {
            logger.debug("Nope: ${e}")

            logger.info("Loading env from file")
            FileInputStream(envFile).use {
                prop.load(it)
            }
        }
    }

    fun get(key: String): String? {
        return prop.getProperty(key) ?: System.getenv(key)
    }
}