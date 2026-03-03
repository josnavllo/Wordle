package org.example.server.src

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import server.src.network.ClientHandler // Import actualizado a la nueva carpeta
import org.example.server.src.config.ServerConfig
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicInteger

fun main() = runBlocking { // Envolvemos todo en un runBlocking para poder lanzar corrutinas

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

        // Usamos una corrutina en el hilo de IO para manejar la concurrencia
        launch(Dispatchers.IO) {
            handler.run()
        }
    }
}