package cloudsearch.util

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.UnknownHostException

typealias NetworkListener = (Boolean) -> Unit

object Network {
    var online: Boolean = true
    val log = LoggerFactory.getLogger(javaClass.name)

    init {
        GlobalScope.launch {
            while (true) {
                try {
                    InetAddress.getByName("www.google.com").isReachable(1000)
                    notifyChanged(true)
                } catch (e: UnknownHostException) {
                    notifyChanged(false)
                }

                delay(5000)
            }
        }
    }

    private fun notifyChanged(newState: Boolean) {
        if (newState != online) {
            online = newState
            val toRemove = mutableListOf<NetworkListener>()
            listeners.forEach {
                try {
                    it.invoke(newState)
                } catch (e: IllegalStateException) {
                    log.info("Removing listener: ${it}: ${e.message}")
                    toRemove.add(it)
                }
            }

            listeners.removeAll(toRemove)
        }
    }

    fun notify(e: UnknownHostException) {
        online = false
    }


    private val listeners = mutableListOf<NetworkListener>()
    fun addListener(onChange: NetworkListener) {
        listeners.add(onChange)
    }

}