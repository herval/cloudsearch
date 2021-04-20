package cloudsearch.api

import cloudsearch.clients.http.OfflineException
import cloudsearch.search.OnlyMostRecentSearch
import cloudsearch.search.Query
import cloudsearch.storage.AccountConfig
import cloudsearch.util.Network
import cloudsearch.util.Uuid
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketException
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import org.slf4j.LoggerFactory

@WebSocket
class SearchSocket(
        val parser: Gson,
        val searcher: OnlyMostRecentSearch
) {
    private val logger = LoggerFactory.getLogger(javaClass.name)

    private fun search(
            query: Query,
            searchId: String,
            socket: Session
    ) = runBlocking {
        try {
            searcher.search(
                    query = query,
                    searchId = searchId,
                    onFinish = { account, quantity ->
                        send(
                                socket,
                                done(
                                        q = quantity,
                                        account = account,
                                        searchId = searchId
                                )
                        )
                    },
                    onFinishAll = { quantity ->
                        send(socket, done(quantity, null, searchId))
                    },
                    onResult = { result ->
                        send(
                                socket,
                                result(result.toJson(query), searchId)
                        )
                    },
                    scope = CoroutineScope(Dispatchers.IO)
            )
        } catch (e: OfflineException) {
            send(socket, error("offline_mode", searchId))
        } catch (e: Exception) {
            logger.error("UNEXPECTED: ${e}", e)
        }

    }

    private fun done(q: Int, account: AccountConfig?, searchId: String): String {
        return parser.toJson(
                mapOf(
                        "type" to "status",
                        "searchId" to searchId,
                        "status" to "done",
                        "totalResults" to q,
                        "accountId" to account?.id,
                        "accountType" to account?.type
                ).filterValues { it != null }
        )
    }

    private fun result(res: Any, searchId: String): String {
        return parser.toJson(
                mapOf(
                        "type" to "result",
                        "result" to res,
                        "searchId" to searchId
                )
        )
    }

    fun error(msg: String, searchId: String?): String {
        return parser.toJson(
                mapOf(
                        "type" to "error",
                        "error" to msg,
                        "searchId" to searchId
                )
        )

    }

    fun notification(msg: String): String {
        return parser.toJson(
                mapOf(
                        "type" to "notification",
                        "notification" to msg
                )
        )
    }


    @Suppress("UNREACHABLE_CODE")
    @OnWebSocketConnect
    fun onConnect(user: Session) {
        logger.info("CONNECTED!")
        Network.addListener { online ->
            if (!online) {
                send(user, error("offline_mode"))
            } else {
                send(user, notification("online_mode"))
            }
        }
    }

    @OnWebSocketClose
    fun oClose(user: Session, statusCode: Int, reason: String) {
        logger.info("CLOSED!")
    }

    @OnWebSocketMessage
    fun onMessage(user: Session, message: String) {
        try {
            val data = parser.fromJson(message, Map::class.java)
            val op = data["op"] as String
            val searchId = data["searchId"] as? String ?: Uuid()

            when (op) {
                "cancel" -> {
                    searcher.stop()
                }
                "search" -> {
                    if (searcher.accountStorage.count() == 0) {
                        send(
                                user,
                                error("No account configured!", searchId)
                        )
                        return
                    }

                    val query = Query.parsed(
                            data["query"] as? String
                    )

                    if (query != null) {
                        GlobalScope.launch {
                            search(
                                    query,
                                    searchId,
                                    user
                            )
                        }
                    } else {
                        send(
                                user,
                                error("You need to specify a query", searchId)
                        )
                    }
                }
            }
        } catch (e: WebSocketException) {
            logger.debug("Socket closed: ${e.message}")
        } catch (e: RuntimeException) {
            logger.error("*** RUNTIME: ${e.message}", e)
        } catch (e: Exception) {
            logger.error("*** EXCEPTION: ${e.message}", e)
        }
    }

    private fun send(user: Session, result: String) {
        user.remote.sendStringByFuture(result)
    }

}