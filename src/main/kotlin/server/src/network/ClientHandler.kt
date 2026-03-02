package org.example.server.network

import com.google.gson.Gson
import org.example.server.src.records.RecordManager
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

    private val gson = Gson()

    override fun run() {
        try {
            val input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val output = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))

            // Función de ayuda para enviar objetos como JSON
            fun sendMessage(msg: NetworkMessage) {
                output.write(gson.toJson(msg))
                output.newLine()
                output.flush()
            }

            // Ignoramos el mensaje inicial del cliente
            input.readLine()

            // Enviar bienvenida
            sendMessage(NetworkMessage(type = "WELCOME", payload = "Hola cliente"))

            var word = ""
            var attempts = 0
            val maxAttempts = 6 // Máximo de intentos permitidos en Wordle

            while (true) {
                val msgJson = input.readLine() ?: break

                // Convertir el JSON recibido a objeto
                val msg = try {
                    gson.fromJson(msgJson, NetworkMessage::class.java)
                } catch (e: Exception) { continue }

                when (msg.type) {
                    "GET_RECORDS" -> {
                        // Enviar el JSON de records incrustado en el payload
                        val recordsStr = RecordManager.getRecordsJson()
                        sendMessage(NetworkMessage(type = "RECORDS_DATA", payload = recordsStr))
                    }
                    "START_GAME" -> {
                        word = pickRandomWord("easy.txt", 5)
                        attempts = 0
                        sendMessage(NetworkMessage(type = "START_GAME", mode = "PVE", wordLength = 5, rounds = 1))
                        println("Partida PVE iniciada con palabra secreta: $word")
                    }
                    "GUESS" -> {
                        if (word.isEmpty()) continue
                        attempts++
                        val guessedWord = msg.word?.uppercase() ?: continue

                        if (guessedWord.length != word.length) {
                            sendMessage(NetworkMessage(type = "ERROR", payload = "La palabra debe tener ${word.length} letras"))
                            continue
                        }

                        // Comprobar letras
                        val resultList = guessedWord.mapIndexed { index, c ->
                            when {
                                word[index] == c -> LetterResult(c.toString(), "CORRECT")
                                word.contains(c) -> LetterResult(c.toString(), "PRESENT")
                                else -> LetterResult(c.toString(), "ABSENT")
                            }
                        }

                        sendMessage(NetworkMessage(type = "GUESS_RESULT", word = guessedWord, result = resultList))

                        if (guessedWord == word) {
                            sendMessage(NetworkMessage(type = "ROUND_WINNER", player = "CLIENT", attempts = attempts, word = word))
                            RecordManager.recordWinPVE("Global") // Guardamos victoria
                            word = "" // Reiniciamos palabra para evitar bugs
                        } else if (attempts >= maxAttempts) {
                            sendMessage(NetworkMessage(type = "ERROR", payload = "¡Te quedaste sin intentos! La palabra era: $word"))
                            RecordManager.recordLossPVE("Global") // Guardamos derrota
                            word = ""
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Error de conexión: ${e.message}")
        } finally {
            clientSocket.close()
            val remaining = activeClients.decrementAndGet()
            println("Cliente desconectado. Activos: $remaining")
        }
    }

    private fun pickRandomWord(filename: String, length: Int): String {
        val stream = javaClass.getResourceAsStream("/dictionary/$filename")
            ?: throw RuntimeException("No se encontró $filename en resources/dictionary")
        val words = stream.bufferedReader().readLines().filter { it.length == length }
        if (words.isEmpty()) throw RuntimeException("No hay palabras en $filename")
        return words.random().uppercase()
    }
}