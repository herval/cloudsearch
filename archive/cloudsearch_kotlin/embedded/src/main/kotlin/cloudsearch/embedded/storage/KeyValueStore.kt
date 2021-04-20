package cloudsearch.embedded.storage

import cloudsearch.storage.Identifiable
import cloudsearch.storage.Storage
import com.googlecode.cqengine.ConcurrentIndexedCollection
import com.googlecode.cqengine.persistence.disk.DiskPersistence
import com.googlecode.cqengine.query.QueryFactory.attribute
import com.googlecode.cqengine.query.QueryFactory.equal
import com.googlecode.cqengine.query.simple.In
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.reflect.KClass


/**
 * Created by herval on 12/9/17.
 */
open class KeyValueStore<T : Identifiable>(
        cachedType: KClass<T>,
        filename: String,
        val storage: StorageConfig,
        val tolerateMissedWrites: Boolean = false,
        cleanupOnFail: Boolean = true
) : Storage<T> {
    private val idField = attribute<T, String>(cachedType.java, String::class.java, "id", { t -> t.id })

    private val logger = LoggerFactory.getLogger(javaClass.name)

    protected val data = ConcurrentIndexedCollection<T>(
            DiskPersistence.onPrimaryKeyInFile(idField, File(storage.storagePath, "${filename}.dat"))
    )

    init {
        if (cleanupOnFail) {
            try {
                first()
            } catch (e: IllegalStateException) {
                logger.info("Cache schema seems to have changed - clearing ${cachedType} storage")
                clear()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun delete(res: T): Boolean {
        if (res as? T != null) {
            return deleteById(res.id)
        } else {
            throw IllegalArgumentException("Trying to delete wrong type: ${res}")
        }
    }

    fun first(): T? {
        return data.iterator().use {
            if (it.hasNext()) {
                it.next()
            }
            null
        }
    }

    override fun deleteById(id: String): Boolean {
        return synchronized(data) {
            data.removeIf { it.id == id }
        }
    }

    override fun deleteAllById(keys: List<String>) {
        synchronized(data) {
            data.removeIf { keys.contains(it.id) }
        }
    }

    final override fun clear() {
        synchronized(data) {
            data.clear()
        }
    }

    override fun getAll(): List<T> {
        return data.toList()
    }

    fun get(ids: List<String>): List<T> {
        return data.retrieve(In(idField, false, ids.toSet())).distinct()
    }

    override fun get(id: String): T? {
        return data.retrieve(equal(idField, id)).singleOrNull()
    }

    fun <R : T> saveOrUpdate(id: String, res: R): Boolean {
        return saveOrUpdate(res) // ignoring ID
    }

    @Suppress("UNCHECKED_CAST")
    override fun saveOrUpdate(res: T): Boolean {
        synchronized(data) {
            if (res as? T != null) {
                //            return try {
                return data.update(
                        listOf(get(res.id)).filterNotNull(),
                        listOf(res)
                )
            } else {
                throw IllegalArgumentException("Trying to save the wrong type: ${res}")
            }
//            } catch (e: SQLiteException) {
//                if (e.resultCode == SQLiteErrorCode.SQLITE_BUSY && tolerateMissedWrites) {
//                    logger.debug("Couldn't saveOrUpdate, safely ignoring")
//                    return true
//                } else {
//                    logger.error("Couldn't saveOrUpdate!", e)
//                    throw e
//                }
//            }
        }
    }

    override fun count(): Int {
        return data.count()
    }
}

