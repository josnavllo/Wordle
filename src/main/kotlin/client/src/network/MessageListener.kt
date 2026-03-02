package org.example.client.src.network

import com.google.gson.Gson
import java.io.BufferedReader

class MessageListener(private val input: BufferedReader) : Runnable {
    @Volatile
    private var running = true
    private val gson = Gson()

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

    private fun handleMessage(jsonResponse: String) {
        try {
            val message = gson.fromJson(jsonResponse, NetworkMessage::class.java)

            when (message.type) {
                "WELCOME", "START_GAME" -> {
                    // 🔹 Los silenciamos por consola para no romper el diseño del menú principal
                }
                "GUESS_RESULT" -> {
                    // Formateamos el resultado de la palabra
                    val resultString = message.result?.joinToString(" ") {
                        "${it.letter}(${it.status.substring(0, 1)})" // Ejemplo: P(C) E(C) R(C) R(C) O(C)
                    }
                    println("\nResultado: $resultString")
                    print("> ")
                }
                "ROUND_WINNER" -> {
                    println("\n🎉 ¡HAS GANADO! La palabra era ${message.word} en ${message.attempts} intentos. 🎉")
                    print("> ")
                }
                "ERROR" -> {
                    println("\n❌ Error: ${message.payload}")
                    print("> ")
                }
                else -> {
                    println("\n[Servidor]: ${message.payload ?: jsonResponse}")
                    print("> ")
                }
            }
        } catch (e: Exception) {
            // Si llega un JSON mal formado o un texto puro, lo imprimimos tal cual
            println("\n[Mensaje Raw]: $jsonResponse")
            print("> ")
        }
    }

    fun stop() {
        running = false
    }
}