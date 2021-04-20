package cloudsearch.storage

import cloudsearch.account.AccountType
import cloudsearch.util.md5


fun idFor(type: AccountType, originalId: String) = md5("${type.name}_${originalId}")


data class AccountConfig(
        val active: Boolean = true,
        val originalId: String,
        val username: String, // usually an email
        val groupName: String? = null, // for services with logical groups (eg slack)
        val expiresAt: Long? = null,
        val credentials: String,
        val apiServer: String? = null,
        val type: AccountType,
        val refreshCredentials: String? = null,
        val scopes: List<String> = emptyList(),
        override val id: String = idFor(type, originalId)
) : Identifiable {

    fun toJson(): Map<String, Any> = mapOf(
            "id" to id,
            "name" to username,
            "type" to type.name,
            "active" to active
    )
}
