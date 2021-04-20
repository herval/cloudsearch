@file:Suppress("UNCHECKED_CAST")

package cloudsearch.cache

import cloudsearch.storage.Identifiable
import cloudsearch.storage.Storage

// not-very-safe in-memory storage
@Suppress("UNCHECKED_CAST")
open class InMemoryStorage<T>(val maxSize: Int = 100) : Storage<T> {
    private val recentlyUpdated = mutableListOf<String>()
    private val data = mutableMapOf<String, Any>()

    override fun count(): Int {
        return data.size
    }

    override fun deleteById(id: String): Boolean {
        return data.remove(id) != null
    }

    override fun deleteAllById(keys: List<String>) {
        keys.forEach { data.remove(it) }
    }

    override fun clear() {
        data.clear()
    }

    override fun get(id: String): T? {
        return data.get(id) as? T
    }

    override fun saveOrUpdate(res: T): Boolean {
        if (res as? Identifiable != null) {
            data.put(res.id, res)
            return true
        } else {
            throw IllegalArgumentException()
        }
    }

    fun <R> saveOrUpdate(id: String, res: R): Boolean {
        if (res as? T != null) {
            data.put(id, res)
            recentlyUpdated.remove(id)
            recentlyUpdated.add(id)

            // prune
            if (recentlyUpdated.size > maxSize) {
                recentlyUpdated.dropLast(recentlyUpdated.size - maxSize).forEach {
                    data.remove(it)
                }
            }
            return true
        } else {
            throw IllegalArgumentException()
        }
    }

    override fun delete(res: T): Boolean {
        if (res as? Identifiable != null) {
            recentlyUpdated.remove(res.id)
            return data.remove(res.id) != null
        } else {
            throw IllegalArgumentException()
        }
    }

    override fun getAll(): List<T> {
        return data.values.toList() as List<T>
    }
}