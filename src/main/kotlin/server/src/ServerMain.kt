package org.example.server.src

import org.example.server.network.ClientHandler
import org.example.server.src.config.ServerConfig
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicInteger
import java.lang.Thread

fun main() {

    val config = ServerConfig.load()
    val serverSocket = ServerSocket(config.port)
    val activeClients = AtomicInteger(0)

    println("Servidor iniciado en ${config.host}:${config.port}")
    println("Máximo de clientes: ${config.maxClients}")

    while (true) {
        val clientSocket = serverSocket.accept()

        if (activeClients.get() >= config.maxClients) {
            println("Servidor lleno. Conexión rechazada.")
            val output = clientSocket.getOutputStream().bufferedWriter()
            output.write("""{"type":"ERROR","payload":"Servidor lleno"}""")
            output.newLine()
            output.flush()
            clientSocket.close()
            continue
        }

        activeClients.incrementAndGet()
        println("Cliente aceptado. Activos: ${activeClients.get()}")

        val handler = ClientHandler(clientSocket, activeClients)
        Thread(handler).start()
    }
}