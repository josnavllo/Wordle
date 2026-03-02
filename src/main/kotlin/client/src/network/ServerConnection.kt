package org.example.client.src.network

import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket

class ServerConnection(private val host: String, private val port: Int) {
    private var socket: Socket? = null
    private var output: BufferedWriter? = null
    private var input: BufferedReader? = null
    private var listener: MessageListener? = null

    fun connect() {
        socket = Socket(host, port)
        output = socket!!.getOutputStream().bufferedWriter()
        input = socket!!.getInputStream().bufferedReader()

        // Arrancamos el hilo que estará siempre escuchando al servidor
        listener = MessageListener(input!!)
        Thread(listener).start()
    }

    fun sendMessage(message: String) {
        output?.write(message)
        output?.newLine()
        output?.flush()
    }

    fun disconnect() {
        listener?.stop()
        socket?.close()
    }
}