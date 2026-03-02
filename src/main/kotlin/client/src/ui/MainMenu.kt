package org.example.client.src.ui

import org.example.client.src.network.ServerConnection
import kotlin.system.exitProcess

class MainMenu(private val connection: ServerConnection) {

    fun show() {
        while (true) {
            println("\n=== WORDLE MULTIJUGADOR ===")
            println("1. Nueva Partida PVP (contra otro jugador)")
            println("2. Nueva Partida PVE (contra la IA)")
            println("3. Ver Records")
            println("4. Configuración")
            println("5. Salir")
            print("Elige una opción: ")

            when (readlnOrNull()?.trim()) {
                "1" -> {
                    println("Buscando partida PVP...")
                    connection.sendMessage("""{"type":"JOIN_QUEUE","mode":"PVP"}""")
                    Thread.sleep(1000) // Pausa breve para ver la respuesta del servidor
                }
                "2" -> {
                    println("Iniciando partida PVE...")
                    connection.sendMessage("""{"type":"START_GAME","mode":"PVE"}""")
                    playGame() // Entramos al bucle de enviar palabras
                }
                "3" -> {
                    println("Solicitando records...")
                    connection.sendMessage("""{"type":"GET_RECORDS"}""")
                    Thread.sleep(1000)
                }
                "4" -> {
                    println("Configuración en construcción...")
                    Thread.sleep(500)
                }
                "5" -> {
                    println("¡Hasta pronto!")
                    connection.disconnect()
                    exitProcess(0)
                }
                else -> println("Opción no válida. Inténtalo de nuevo.")
            }
        }
    }

    private fun playGame() {
        println("--- MODO JUEGO INICIADO ---")
        println("(Escribe 'salir' en cualquier momento para volver al menú)")
        var attempts = 0

        while (true) {
            print("> ")
            val guess = readlnOrNull()?.uppercase()?.trim() ?: continue

            if (guess == "SALIR") {
                println("Abandonando la partida...")
                break
            }

            if (guess.length != 5) {
                println("La palabra debe tener exactamente 5 letras.")
                continue
            }

            attempts++
            // Enviamos el intento al servidor
            connection.sendMessage("""{"type":"GUESS","word":"$guess","attempt":$attempts}""")

            // Pausa breve para dar tiempo a que MessageListener imprima la respuesta
            Thread.sleep(200)
        }
    }
}