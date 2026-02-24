package org.example.server.network

import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class ClientHandler(
    private val clientSocket: Socket,
    private val activeClients: AtomicInteger
) : Runnable {

    override fun run() {
        try {
            val input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val output = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))

            // Recibir mensaje inicial del cliente
            val line = input.readLine()
            println("Recibido: $line")

            // Enviar bienvenida
            output.write("""{"type":"WELCOME","payload":"Hola cliente"}""")
            output.newLine()
            output.flush()

            // Mantener conexión activa (para probar múltiples clientes)
            println("Cliente conectado. Manteniendo conexión abierta 20 segundos...")
            Thread.sleep(20000)  // Solo para pruebas
            println("Conexión con cliente finalizada")

        } catch (e: Exception) {
            println("Error en cliente: ${e.message}")
        } finally {
            clientSocket.close()
            val remaining = activeClients.decrementAndGet()
            println("Cliente desconectado. Activos: $remaining")
        }
    }
}