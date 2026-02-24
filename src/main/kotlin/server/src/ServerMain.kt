package org.example.server.src

import org.example.server.src.config.ServerConfig
import java.io.File
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicInteger

fun main() {
    val config = ServerConfig.load()
    val serverSocket = ServerSocket(config.port)

    println(File(".").absolutePath)

    println("Servidor iniciado en ${config.host}:${config.port}")
    println("Máximo de clientes: ${config.maxClients}")

    val activeClients = AtomicInteger(0)

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

        println("Cliente conectado. Activos: ${activeClients.get()}")

        Thread {

            try {
                val input = clientSocket.getInputStream().bufferedReader()
                val output = clientSocket.getOutputStream().bufferedWriter()

                val line = input.readLine()
                println("Recibido: $line")

                output.write("""{"type":"WELCOME","payload":"Hola cliente"}""")
                output.newLine()
                output.flush()


            } finally {
                clientSocket.close()
                activeClients.decrementAndGet()
                println("Cliente desconectado. Activos: ${activeClients.get()}")
            }

        }.start()
    }
}
