package org.example.client.src.network

import java.io.BufferedReader

class MessageListener(private val input: BufferedReader) : Runnable {
    @Volatile
    private var running = true

    override fun run() {
        try {
            while (running) {
                val response = input.readLine() ?: break
                handleMessage(response)
            }
        } catch (e: Exception) {
            if (running) println("\n[Desconectado del servidor]")
        }
    }

    private fun handleMessage(message: String) {
        // En los siguientes pasos usaremos JSON para procesar esto mejor
        println("\n[Servidor]: $message")

        if (message.contains("ROUND_WINNER")) {
            println("🎉 ¡HAS GANADO! 🎉")
        } else if (message.contains("ERROR")) {
            println("❌ Error: Algo ha ido mal con la última acción.")
        }

        print("> ") // Restaura el cursor para que el usuario sepa que puede escribir
    }

    fun stop() {
        running = false
    }
}