package org.example.client.src

import java.net.Socket

fun main() {
    val socket = Socket("localhost", 5678)

    val output = socket.getOutputStream().bufferedWriter()
    val input = socket.getInputStream().bufferedReader()

    // Enviar mensaje inicial
    output.write("""{"type":"HELLO","payload":"Soy el cliente"}""")
    output.newLine()
    output.flush()

    // Leer mensajes iniciales hasta START_GAME
    while (true) {
        val response = input.readLine() ?: break
        println("Servidor dice: $response")
        if (response.contains("START_GAME")) break
    }

    // 🔹 Bucle de adivinanzas
    var attempts = 0
    gameLoop@ while (true) {
        print("Introduce tu palabra (5 letras): ")
        val guess = readLine()?.uppercase() ?: continue
        attempts++

        // Enviar intento al servidor
        output.write("""{"type":"GUESS","word":"$guess","attempt":$attempts}""")
        output.newLine()
        output.flush()

        // 🔹 Leer respuestas del servidor hasta recibir ROUND_WINNER o ERROR
        while (true) {
            val response = input.readLine() ?: break
            println("Servidor dice: $response")

            if (response.contains("ROUND_WINNER")) {
                println("¡Has ganado! 🎉")
                break@gameLoop
            }

            if (response.contains("ERROR")) {
                println("Error del servidor. Intenta otra palabra.")
                break // salir de este bucle para pedir otra palabra
            }

            // GUESS_RESULT -> mostrarlo y salir del bucle interno para permitir siguiente palabra
            if (response.contains("GUESS_RESULT")) break
        }
    }

    println("Juego terminado. Pulsa ENTER para cerrar...")
    readlnOrNull()
    socket.close()
}