package cloudsearch.config

import cloudsearch.account.AccountType

/**
 * Created by herval on 12/14/17.
 */
data class ServiceCredentials(
        private val perService: Map<AccountType, Credentials>
) {
    fun get(service: AccountType): Credentials {
        return perService[service]!!
    }

    companion object {
        fun fromConfig(conf: Config): ServiceCredentials {

            return ServiceCredentials(
                    AccountType.values().map { t ->
                        t to credentialsFor(conf, t.name)
                    }.toMap()
            )
        }

        private fun credentialsFor(conf: Config, service: String): Credentials {
            return Credentials(
                    conf.get("${service.toUpperCase()}_APP_NAME")!!,
                    conf.get("${service.toUpperCase()}_CLIENT_ID")!!,
                    conf.get("${service.toUpperCase()}_CLIENT_SECRET")!!
            )
        }
    }
}

data class Credentials(
        val appName: String,
        val clientId: String,
        val clientSecret: String
)