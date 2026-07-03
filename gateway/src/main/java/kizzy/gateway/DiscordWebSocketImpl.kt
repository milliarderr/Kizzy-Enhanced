package kizzy.gateway

import com.my.kizzy.domain.interfaces.Logger
import com.my.kizzy.domain.interfaces.NoOpLogger
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kizzy.gateway.entities.Heartbeat
import kizzy.gateway.entities.Identify.Companion.toIdentifyPayload
import kizzy.gateway.entities.OutgoingPayload
import kizzy.gateway.entities.Payload
import kizzy.gateway.entities.PayloadData
import kizzy.gateway.entities.Ready
import kizzy.gateway.entities.Resume
import kizzy.gateway.entities.op.OpCode
import kizzy.gateway.entities.op.OpCode.*
import kizzy.gateway.entities.presence.Presence
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

open class DiscordWebSocketImpl(
    private val token: String,
    private val logger: Logger = NoOpLogger,
    private val identifyBrowser: String = "Discord Client",
    private val identifyOs: String = "Windows",
    private val identifyDevice: String = "ktor"
) : DiscordWebSocket {
    private val gatewayUrl = "wss://gateway.discord.gg/?v=10&encoding=json"
    private var websocket: DefaultClientWebSocketSession? = null
    private var sequence = 0
    private var sessionId: String? = null
    private var heartbeatInterval = 0L
    private var resumeGatewayUrl: String? = null
    private var heartbeatJob: Job? = null
    private var connected = false
    private var deliberateClose = false
    private var reconnectAttempts = 0
    private var client: HttpClient = HttpClient {
        install(WebSockets)
    }
    private val json = Json{
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Default

    override suspend fun connect() {
        launch {
            try {
                logger.i("Gateway","Connect called")
                val url = resumeGatewayUrl ?: gatewayUrl
                websocket = client.webSocketSession(url)
                reconnectAttempts = 0

                // start receiving messages
                websocket!!.incoming.receiveAsFlow()
                    .collect {
                        when (it) {
                            is Frame.Text -> {
                                val jsonString = it.readText()
                                onMessage(jsonString)
                            }
                            else -> {}
                        }
                    }
                handleClose()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.e("Gateway",e.message?:"")
                scheduleReconnect()
            }
        }
    }

    private suspend fun handleClose() {
        heartbeatJob?.cancel()
        connected = false
        val close = websocket?.closeReason?.await()
        val code = close?.code?.toInt()
        logger.w("Gateway","Closed with code: $code, reason: ${close?.message}")
        when {
            deliberateClose -> {}
            code == 4000 -> {
                delay(200.milliseconds)
                connect()
            }
            code == 4004 || (code != null && code in 4010..4014) -> {
                logger.e("Gateway","Fatal close code $code — not reconnecting")
                close()
            }
            else -> scheduleReconnect()
        }
    }

    private suspend fun scheduleReconnect() {
        if (deliberateClose) return
        heartbeatJob?.cancel()
        connected = false
        runCatching { websocket?.close() }
        val delayMs = (2000L shl minOf(reconnectAttempts, 5)).coerceAtMost(60_000L)
        reconnectAttempts++
        if (reconnectAttempts >= 3) {
            resumeGatewayUrl = null
            sessionId = null
        }
        logger.w("Gateway","Connection lost — reconnecting in ${delayMs}ms (attempt $reconnectAttempts)")
        delay(delayMs)
        if (!deliberateClose) connect()
    }

    private suspend fun onMessage(jsonString: String) {
        val payload = json.decodeFromString<Payload>(jsonString)
        logger.d("Gateway","Received op:${payload.op}, seq:${payload.s}, event :${payload.t}")

        payload.s?.let {
            sequence = it
        }
        when (payload.op) {
            DISPATCH -> payload.handleDispatch(jsonString)
            HEARTBEAT -> sendHeartBeat()
            RECONNECT -> reconnectWebSocket()
            INVALID_SESSION -> handleInvalidSession()
            HELLO -> handleHello(jsonString)
            else -> {}
        }
    }

    open fun Payload.handleDispatch(jsonString: String) {
        when (this.t.toString()) {
            "READY" -> {
                val ready = decodePayloadData<Ready>(jsonString) ?: return
                sessionId = ready.sessionId
                resumeGatewayUrl = ready.resumeGatewayUrl + "/?v=10&encoding=json"
                logger.i("Gateway","resume_gateway_url updated to $resumeGatewayUrl")
                logger.i("Gateway","session_id updated to $sessionId")
                connected = true
                return
            }
            "RESUMED" -> {
                logger.i("Gateway","Session Resumed")
            }
            else -> {}
        }
    }

    private suspend inline fun handleInvalidSession() {
        logger.i("Gateway","Handling Invalid Session")
        logger.d("Gateway","Sending Identify after 150ms")
        delay(150)
        sendIdentify()
    }

    private suspend inline fun handleHello(jsonString: String) {
        if (sequence > 0 && !sessionId.isNullOrBlank()) {
            sendResume()
        } else {
            sendIdentify()
        }
        heartbeatInterval = decodePayloadData<Heartbeat>(jsonString)?.heartbeatInterval ?: return
        logger.i("Gateway","Setting heartbeatInterval= $heartbeatInterval")
        startHeartbeatJob(heartbeatInterval)
    }

    protected fun decodeReady(jsonString: String): Ready? {
        return decodePayloadData(jsonString)
    }

    private inline fun <reified T> decodePayloadData(jsonString: String): T? {
        return json.decodeFromString<PayloadData<T>>(jsonString).d
    }

    private suspend fun sendHeartBeat() {
        logger.i("Gateway","Sending $HEARTBEAT with seq: $sequence")
        send(
            op = HEARTBEAT,
            d = if (sequence == 0) "null" else sequence.toString(),
        )
    }

    private suspend inline fun reconnectWebSocket() {
        websocket?.close(
            CloseReason(
                code = 4000,
                message = "Attempting to reconnect"
            )
        )
    }

    private suspend fun sendIdentify() {
        logger.i("Gateway","Sending $IDENTIFY")
        send(
            op = IDENTIFY,
            d = token.toIdentifyPayload(
                browser = identifyBrowser,
                os = identifyOs,
                device = identifyDevice
            )
        )
    }

    private suspend fun sendResume() {
        logger.i("Gateway","Sending $RESUME")
        send(
            op = RESUME,
            d = Resume(
                seq = sequence,
                sessionId = sessionId,
                token = token
            )
        )
    }

    private fun startHeartbeatJob(interval: Long) {
        heartbeatJob?.cancel()
        heartbeatJob = launch {
            while (isActive) {
                sendHeartBeat()
                delay(interval)
            }
        }
    }

    private fun isSocketConnectedToAccount(): Boolean {
        return connected && websocket?.isActive == true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun isWebSocketConnected(): Boolean {
        return websocket?.incoming != null && websocket?.outgoing?.isClosedForSend == false
    }

    private suspend inline fun <reified T> send(op: OpCode, d: T?) {
        if (websocket?.isActive == true) {
            val payload = json.encodeToString(
                OutgoingPayload(
                    op = op,
                    d = d,
                )
            )
            websocket?.send(Frame.Text(payload))
        }
    }

    override fun close() {
        deliberateClose = true
        heartbeatJob?.cancel()
        heartbeatJob = null
        this.cancel()
        resumeGatewayUrl = null
        sessionId = null
        connected = false
        runBlocking {
            websocket?.close()
            logger.e("Gateway","Connection to gateway closed")
        }
    }

    override suspend fun sendActivity(presence: Presence) {
        var waitedMs = 0L
        while (!isSocketConnectedToAccount()) {
            delay(50.milliseconds)
            waitedMs += 50
            if (waitedMs >= 60_000) {
                logger.w("Gateway","Socket not connected after 60s — dropping presence update")
                return
            }
        }
        logger.i("Gateway","Sending $PRESENCE_UPDATE")
        send(
            op = PRESENCE_UPDATE,
            d = presence
        )
    }

}
