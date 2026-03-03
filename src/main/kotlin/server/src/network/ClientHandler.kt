package server.src.network

import com.google.gson.Gson
import org.example.server.src.network.LetterResult
import org.example.server.src.network.NetworkMessage
import server.src.dictionary.DictionaryService
import server.src.game.GameManager
import server.src.game.PvPGame
import server.src.records.RecordManager
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger


class ClientHandler(
    private val clientSocket: Socket,
    private val activeClients: AtomicInteger
) : Runnable {

    private val gson = Gson()
    private lateinit var output: BufferedWriter

    var currentGame: PvPGame? = null
    var inPvPMode = false
    var playerName: String = "Anónimo"
    var startTime: Long = 0

    @Synchronized
    fun sendMessage(msg: NetworkMessage) {
        try {
            output.write(gson.toJson(msg))
            output.newLine()
            output.flush()
        } catch (e: Exception) {
            println("Error enviando mensaje a cliente: ${e.message}")
        }
    }

    override fun run() {
        try {
            val input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            output = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))

            sendMessage(NetworkMessage(type = "WELCOME", payload = "Hola cliente"))

            var word = ""
            var attempts = 0
            val maxAttempts = 6

            while (true) {
                val msgJson = input.readLine() ?: break
                val msg = try {
                    gson.fromJson(msgJson, NetworkMessage::class.java)
                } catch (e: Exception) { continue }

                when (msg.type) {
                    "HELLO" -> {
                        playerName = msg.player ?: "Anónimo"
                        println("Se ha conectado el jugador: $playerName")
                    }
                    "GET_RECORDS" -> {
                        sendMessage(NetworkMessage(type = "RECORDS_DATA", payload = RecordManager.getRecordsJson()))
                    }
                    "JOIN_QUEUE" -> {
                        inPvPMode = true
                        val diff = msg.difficulty ?: "EASY"
                        sendMessage(NetworkMessage(type = "INFO", payload = "Buscando oponente para PVP ($diff)..."))
                        GameManager.joinQueue(this, diff)
                    }
                    "START_GAME" -> {
                        inPvPMode = false
                        val diff = msg.difficulty ?: "EASY"
                        word = DictionaryService.pickRandomWord("${diff.lowercase()}.txt", 5)
                        attempts = 0
                        startTime = System.currentTimeMillis()
                        sendMessage(NetworkMessage(type = "START_GAME", mode = "PVE", difficulty = diff, wordLength = 5, rounds = 1))
                    }
                    "GUESS" -> {
                        val guessedWord = msg.word?.uppercase() ?: continue

                        if (inPvPMode && currentGame != null) {
                            currentGame?.processGuess(this, guessedWord, msg.attempt ?: 1)
                        }
                        else if (!inPvPMode) {
                            if (word.isEmpty()) continue
                            attempts++
                            if (guessedWord.length != word.length) {
                                sendMessage(NetworkMessage(type = "ERROR", payload = "La palabra debe tener ${word.length} letras"))
                                continue
                            }

                            // 🔹 NUEVA LÓGICA WORDLE EXACTA (Doble pasada)
                            val resultList = MutableList(word.length) { LetterResult("", "ABSENT") }
                            val charCounts = mutableMapOf<Char, Int>()
                            word.forEach { charCounts[it] = charCounts.getOrDefault(it, 0) + 1 }

                            // Primera pasada: Verdes
                            for (i in guessedWord.indices) {
                                if (guessedWord[i] == word[i]) {
                                    resultList[i] = LetterResult(guessedWord[i].toString(), "CORRECT")
                                    charCounts[guessedWord[i]] = charCounts[guessedWord[i]]!! - 1
                                }
                            }

                            // Segunda pasada: Amarillos
                            for (i in guessedWord.indices) {
                                if (resultList[i].status != "CORRECT") {
                                    val c = guessedWord[i]
                                    if (charCounts.getOrDefault(c, 0) > 0) {
                                        resultList[i] = LetterResult(c.toString(), "PRESENT")
                                        charCounts[c] = charCounts[c]!! - 1
                                    } else {
                                        resultList[i] = LetterResult(c.toString(), "ABSENT")
                                    }
                                }
                            }

                            sendMessage(NetworkMessage(type = "GUESS_RESULT", word = guessedWord, result = resultList))

                            if (guessedWord == word) {
                                val timeSecs = (System.currentTimeMillis() - startTime) / 1000
                                sendMessage(NetworkMessage(type = "ROUND_WINNER", player = playerName, attempts = attempts, word = word))
                                RecordManager.recordWinPVE(playerName, attempts, timeSecs)
                                word = ""
                            } else if (attempts >= maxAttempts) {
                                val timeSecs = (System.currentTimeMillis() - startTime) / 1000
                                sendMessage(NetworkMessage(type = "ERROR", payload = "¡Te quedaste sin intentos! La palabra era: $word"))
                                RecordManager.recordLossPVE(playerName, timeSecs)
                                word = ""
                            }
                        }
                    }
                    "LEAVE_GAME" -> {
                        GameManager.leaveQueue(this)
                        currentGame?.playerDisconnected(this)
                        inPvPMode = false
                    }
                }
            }
        } catch (e: Exception) {
            println("Conexión perdida con un cliente.")
        } finally {
            GameManager.leaveQueue(this)
            currentGame?.playerDisconnected(this)
            clientSocket.close()
            activeClients.decrementAndGet()
        }
    }
}