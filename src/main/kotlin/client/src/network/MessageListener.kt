package client.src.network

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import client.src.ui.GameState
import client.src.ui.WordleUI
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
                "WELCOME" -> { }
                "START_GAME" -> {
                    WordleUI.reset(pvp = message.mode == "PVP")
                    WordleUI.draw()
                    print("\nEscribe tu palabra > ")
                }
                "INFO" -> {
                    println("\n[Info]: ${message.payload}")
                    print("> ")
                }
                "GUESS_RESULT" -> {
                    message.result?.let { WordleUI.addAttempt(it) }
                    WordleUI.draw()
                    print("\nEscribe tu palabra > ")
                }
                "OPPONENT_PROGRESS" -> {
                    WordleUI.opponentAttempts = message.attempts ?: 0
                    WordleUI.draw()
                    print("\nEscribe tu palabra > ")
                }
                "ROUND_WINNER" -> {
                    println("\n🎉 ¡HAS GANADO! La palabra era ${message.word}. (Intentos: ${message.attempts}) 🎉")
                    println("👉 Pulsa ENTER para volver al menú principal.")
                    GameState.isGameActive = false
                }
                "RECORDS_DATA" -> {
                    println("\n=== 🏆 RECORDS Y ESTADÍSTICAS DEL SERVIDOR 🏆 ===")
                    try {
                        val statsArray = gson.fromJson(message.payload, JsonArray::class.java)
                        for (element in statsArray) {
                            val stats = element as JsonObject
                            val name = stats.get("playerName").asString
                            val played = stats.get("gamesPlayed")?.asInt ?: 0
                            val wonPVE = stats.get("gamesWonPVE")?.asInt ?: 0
                            val wonPVP = stats.get("gamesWonPVP")?.asInt ?: 0
                            val maxStreak = stats.get("maxStreak")?.asInt ?: 0
                            val totalTime = stats.get("totalTimeSeconds")?.asLong ?: 0L

                            // 🔹 Calculamos las medias y porcentajes aquí directamente
                            val winPct = if (played > 0) ((wonPVE + wonPVP).toDouble() / played) * 100.0 else 0.0
                            val avgTime = if (played > 0) totalTime.toDouble() / played else 0.0

                            // Redondeamos a 1 decimal de forma segura
                            val roundPct = Math.round(winPct * 10.0) / 10.0
                            val roundAvg = Math.round(avgTime * 10.0) / 10.0

                            val dist = stats.getAsJsonObject("guessDistribution")
                            val d1 = dist?.get("1")?.asInt ?: 0
                            val d2 = dist?.get("2")?.asInt ?: 0
                            val d3 = dist?.get("3")?.asInt ?: 0
                            val d4 = dist?.get("4")?.asInt ?: 0
                            val d5 = dist?.get("5")?.asInt ?: 0
                            val d6 = dist?.get("6")?.asInt ?: 0

                            println("👤 Jugador: $name | Partidas: $played")
                            println("   🟢 PVE Ganadas: $wonPVE | ⚔️ PVP Ganadas: $wonPVP")
                            println("   🔥 Racha Máxima: $maxStreak | ⏱️ Tiempo Medio: ${roundAvg}s | 📈 Victorias: ${roundPct}%")
                            println("   📊 Distribución de aciertos (Intentos):")
                            println("      [1]: $d1  [2]: $d2  [3]: $d3  [4]: $d4  [5]: $d5  [6]: $d6")
                            println("--------------------------------------------------")
                        }
                    } catch (e: Exception) {
                        println("❌ Error procesando los records: ${e.message}")
                    }
                    println("==================================================")
                    print("> ")
                }
                "ERROR" -> {
                    println("\n❌ Error: ${message.payload}")
                    if (message.payload != null && (
                                message.payload.contains("Te quedaste sin intentos") ||
                                        message.payload.contains("Has perdido") ||
                                        message.payload.contains("abandono")
                                )) {
                        GameState.isGameActive = false
                        println("👉 Pulsa ENTER para volver al menú principal.")
                    }
                    print("> ")
                }
                else -> {
                    println("\n[Servidor]: ${message.payload ?: jsonResponse}")
                    print("> ")
                }
            }
        } catch (e: Exception) {
            println("\n[Mensaje Raw]: $jsonResponse")
            print("> ")
        }
    }

    fun stop() {
        running = false
    }
}