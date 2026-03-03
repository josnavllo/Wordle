package client.src.network

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket

class ServerConnection(private val host: String, private val port: Int) {
    private var socket: Socket? = null
    private var output: BufferedWriter? = null
    private var input: BufferedReader? = null
    private var listener: MessageListener? = null
    private val gson = Gson() // Instancia de Gson

    fun connect() {
        socket = Socket(host, port)
        output = socket!!.getOutputStream().bufferedWriter()
        input = socket!!.getInputStream().bufferedReader()

        listener = MessageListener(input!!)
        Thread(listener).start()
    }

    // 🔹 Ahora enviamos objetos en lugar de Strings
    fun sendMessage(message: NetworkMessage) {
        val jsonString = gson.toJson(message)
        output?.write(jsonString)
        output?.newLine()
        output?.flush()
    }

    fun disconnect() {
        listener?.stop()
        socket?.close()
    }
}