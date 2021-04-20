package cloudsearch.server.api

import cloudsearch.account.AccountType
import cloudsearch.auth.OauthRedirectConfig
import cloudsearch.config.Config

data class ServerConfig(
        val port: Int,
        val domain: String
) : OauthRedirectConfig {

    companion object {

        fun fromConfig(c: Config): ServerConfig {
            return ServerConfig(
                    c.get("PORT")!!.toInt(),
                    c.get("DOMAIN")!!
            )
        }

    }

    override fun redirectUrl(type: AccountType): String {
        return "https://${domain}/accounts/oauth/callback/${type.name}"
    }

}