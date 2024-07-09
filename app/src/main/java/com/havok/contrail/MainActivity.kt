package com.havok.contrail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.havok.contrail.ui.theme.ContrailTheme
import com.havok.contrail.ui.theme.HomeScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private lateinit var client: OkHttpClient
    private lateinit var webSocket: WebSocket
    private lateinit var socketIP: String
    private lateinit var socketPort: String
    private lateinit var motorSpeedCoefficient: FloatArray

    private var connected by mutableStateOf(false)
    private var logs = mutableStateOf(listOf<String>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContrailTheme {
                HomeScreen(
                    onButtonClick = { handleConnection() },
                    onActionButtonClick = { action ->
                        sendAction(action)
                        addLog("Action: $action")
                    },
                    onRotStickMoved = { x ->
                        sendRotStickPosition(x)
                        addLog("{\"x\": %.2f}".format(x))
                    },
                    onJoystickMoved = { x, y ->
                        sendJoystickPosition(x, y)
                        addLog("{\"x\": %.2f, \"y\": %.2f}".format(x, y))
                    },
                    logs = logs.value,
                    onSaveClick = ::onSaveClick,
                    ip = socketIP,
                    port = socketPort,
                    motorSpeedCoefficient = motorSpeedCoefficient
                )
            }
        }
        client = OkHttpClient.Builder()
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .build()

        socketIP = "192.168.61.78"
        socketPort = "80"
        motorSpeedCoefficient = floatArrayOf(1f, 1f, 1f, 1f)
    }

    private fun handleConnection(): Boolean {
        if (!connected) {
            val request = Request.Builder().url("ws://$socketIP:${socketPort}/").build()

            webSocket = client.newWebSocket(request, object: WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    updateStatus(true)
                    addLog("Connected ($socketIP:$socketPort)")
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    super.onMessage(webSocket, bytes)
                    // Handle Incoming Bytes
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    // Handle Incoming messages
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosed(webSocket, code, reason)
                    updateStatus(false)
                    addLog("Disconnected: $reason")
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    println("Connection Failed: ${t.message}")
                    response?.let {
                        println("Response: ${it.message}")
                    }
                    updateStatus(false)
                    addLog("Connection Failed: ${t.message}")
                }
            })
        } else {
            webSocket.close(1000, "User initiated")
            addLog("Disconnected by user")
        }

        return connected
    }

    private fun updateStatus(status: Boolean) {
        GlobalScope.launch(Dispatchers.Main) {
            connected = status
        }
    }

    private fun sendAction(action: String) {
        if (connected) {
            val msg = "{\"type\": \"BSTS\", \"action\": \"${action}\"}"
            webSocket.send(msg)
        }
    }

    private fun sendJoystickPosition(x: Float, y: Float) {
        if (connected) {
            val posX: Int = (x*100f).toInt()
            val posY: Int = (y*100f).toInt()
            val position = "{\"type\": \"PSJ\", \"x\": %d, \"y\": %d}".format(posX, posY)
            webSocket.send(position)
        }
    }

    private fun sendRotStickPosition(x: Float) {
        if (connected) {
            val posX: Int = (x*100f).toInt()
            val position = "{\"type\": \"RTJ\", \"x\": %d}".format(posX)
            webSocket.send(position)
        }
    }

    private fun sendMotorSpeedCoefficient(motorSpeedCoefficient: FloatArray) {
        if (connected) {
            val msg = "{\"type\": \"BSTS\",\"action\": \"asc\", \"c1\": %.2f, \"c2\": %.2f, \"c3\": %.2f, \"c4\": %.2f}".format(motorSpeedCoefficient[0], motorSpeedCoefficient[1], motorSpeedCoefficient[2], motorSpeedCoefficient[3])
            webSocket.send(msg)
        }
    }

    private fun addLog(message: String) {
        GlobalScope.launch(Dispatchers.Main) {
            logs.value = logs.value.takeLast(2) + message // Keep last 5 logs
        }
    }

    private fun onSaveClick(ip: String, port: String, fl: Float, fr: Float, br: Float, bl: Float) {
        this.socketIP = ip
        this.socketPort = port
        this.motorSpeedCoefficient[2] = fl
        this.motorSpeedCoefficient[3] = fr
        this.motorSpeedCoefficient[0] = br
        this.motorSpeedCoefficient[1] = bl

        sendMotorSpeedCoefficient(motorSpeedCoefficient)

        addLog("IP changed to $socketIP")
        addLog("Port changed to $socketPort")
        addLog("Coefficient C1: %.2f, C2: %.2f, C3: %.2f, C4: %.2f".format(motorSpeedCoefficient[0], motorSpeedCoefficient[1], motorSpeedCoefficient[2], motorSpeedCoefficient[3]))
    }
}
