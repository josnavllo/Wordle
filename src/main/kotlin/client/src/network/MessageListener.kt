package org.example.client.src.network

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.BufferedReader

class MessageListener(private val input: BufferedReader) : Runnable {
    @Volatile
    private var running = true
    private val gson = Gson()

    // 🔹 Códigos ANSI para colorear la consola 🔹
    private val RESET = "\u001B[0m"
    private val BG_GREEN = "\u001B[42m\u001B[30m"  // Fondo Verde, Letra Negra (CORRECTO)
    private val BG_YELLOW = "\u001B[43m\u001B[30m" // Fondo Amarillo, Letra Negra (PRESENTE)
    private val BG_GRAY = "\u001B[100m\u001B[37m"  // Fondo Gris Oscuro, Letra Blanca (AUSENTE)

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
                    // Silenciados para mantener limpio el menú
                }
                "INFO" -> {
                    println("\n[Info]: ${message.payload}")
                    print("> ")
                }
                "GUESS_RESULT" -> {
                    // 🔹 Aplicamos la magia de los colores aquí 🔹
                    val resultString = message.result?.joinToString(" ") {
                        when (it.status) {
                            "CORRECT" -> "$BG_GREEN ${it.letter} $RESET"
                            "PRESENT" -> "$BG_YELLOW ${it.letter} $RESET"
                            "ABSENT" -> "$BG_GRAY ${it.letter} $RESET"
                            else -> " ${it.letter} "
                        }
                    }
                    println("\nIntento: $resultString")
                    print("> ")
                }
                "ROUND_WINNER" -> {
                    println("\n🎉 ¡HAS GANADO! La palabra era ${message.word}. (Intentos: ${message.attempts}) 🎉")
                    print("> ")
                }
                "RECORDS_DATA" -> {
                    // 🔹 Formateamos el JSON en una tabla legible 🔹
                    println("\n=== 🏆 RECORDS DEL SERVIDOR 🏆 ===")
                    try {
                        val statsArray = gson.fromJson(message.payload, JsonArray::class.java)
                        for (element in statsArray) {
                            val stats = element as JsonObject
                            val name = stats.get("playerName").asString
                            val wonPVE = stats.get("gamesWonPVE")?.asInt ?: 0
                            val wonPVP = stats.get("gamesWonPVP")?.asInt ?: 0
                            val maxStreak = stats.get("maxStreak")?.asInt ?: 0

                            println("👤 Jugador: $name | 🟢 Victorias PVE: $wonPVE | ⚔️ Victorias PVP: $wonPVP | 🔥 Racha Máxima: $maxStreak")
                        }
                    } catch (e: Exception) {
                        println(message.payload) // Fallback por si el JSON viene raro
                    }
                    println("==================================")
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
            // Si no es un JSON o está mal formado
            println("\n[Mensaje Raw]: $jsonResponse")
            print("> ")
        }
    }

    fun stop() {
        running = false
    }
}