package cloudsearch.storage

/**
 * Created by herval on 12/9/17.
 */
interface Storage<T> {
    fun clear()
    fun get(id: String): T?
    fun saveOrUpdate(res: T): Boolean
    fun delete(res: T): Boolean
    fun getAll(): List<T>
    fun deleteById(id: String): Boolean
    fun deleteAllById(keys: List<String>)
    fun count(): Int
}