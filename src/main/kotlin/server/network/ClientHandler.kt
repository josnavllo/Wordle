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

            // 🔹 Iniciar partida PVE
            val word = pickRandomWord("easy.txt", 5)
            output.write("""{"type":"START_GAME","mode":"PVE","wordLength":5,"rounds":1}""")
            output.newLine()
            output.flush()
            println("Partida PVE iniciada con palabra de 5 letras")

            // 🔹 Contador de intentos del cliente
            var attempts = 1

            // 🔹 Bucle de adivinanzas
            while (true) {
                val msg = input.readLine() ?: break
                println("Recibido: $msg")

                if (msg.contains("GUESS")) {
                    attempts++  // contar este intento

                    // Extraer palabra
                    val guessedWord = Regex(""""word"\s*:\s*"([A-Za-z]+)""")
                        .find(msg)?.groupValues?.get(1)?.uppercase() ?: continue

                    // Validar longitud
                    if (guessedWord.length != word.length) {
                        output.write("""{"type":"ERROR","payload":"La palabra debe tener ${word.length} letras"}""")
                        output.newLine()
                        output.flush()
                        continue
                    }

                    // Comparar con la palabra secreta
                    val result = guessedWord.mapIndexed { index, c ->
                        when {
                            word[index] == c -> """{"letter":"$c","status":"CORRECT"}"""
                            word.contains(c) -> """{"letter":"$c","status":"PRESENT"}"""
                            else -> """{"letter":"$c","status":"ABSENT"}"""
                        }
                    }

                    // Enviar resultado al cliente
                    output.write("""{"type":"GUESS_RESULT","word":"$guessedWord","result":[${result.joinToString(",")}] }""")
                    output.newLine()
                    output.flush()

                    // Verificar si ganó
                    if (guessedWord == word) {
                        output.write("""{"type":"ROUND_WINNER","player":"CLIENT","attempts":$attempts,"word":"$word"}""")
                        output.newLine()
                        output.flush()
                        break
                    }
                }
            }

            println("Partida terminada para este cliente")

        } catch (e: Exception) {
            e.printStackTrace()
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
        if (words.isEmpty()) throw RuntimeException("No hay palabras de longitud $length en $filename")
        return words.random().uppercase()
    }
}