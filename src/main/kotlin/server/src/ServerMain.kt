package org.example.server.src

import java.net.ServerSocket

fun main() {
    val port = 5678
    val serverSocket = ServerSocket(port)

    println("Servidor escuchando en puerto $port")

    while (true) {
        val clientSocket = serverSocket.accept()
        println("Cliente conectado")

        Thread {
            val input = clientSocket.getInputStream().bufferedReader()
            val output = clientSocket.getOutputStream().bufferedWriter()

            val line = input.readLine()
            println("Recibido: $line")

            output.write("""{"type":"WELCOME","payload":"Hola cliente"}""")
            output.newLine()
            output.flush()

            clientSocket.close()
        }.start()
    }
}
