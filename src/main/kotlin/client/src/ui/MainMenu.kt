package client.src.ui

import client.src.network.NetworkMessage
import org.example.client.src.network.ServerConnection
import kotlin.system.exitProcess

class MainMenu(private val connection: ServerConnection) {

    private var currentDifficulty = "EASY" // Dificultad por defecto

    fun show() {
        while (true) {
            println("\n=== WORDLE MULTIJUGADOR ===")
            println("1. Nueva Partida PVP (contra otro jugador)")
            println("2. Nueva Partida PVE (contra la IA)")
            println("3. Ver Records")
            println("4. Configuración (Actual: $currentDifficulty)")
            println("5. Salir")
            print("Elige una opción: ")

            when (readlnOrNull()?.trim()) {
                "1" -> {
                    println("Buscando partida PVP ($currentDifficulty)...")
                    connection.sendMessage(
                        NetworkMessage(
                            type = "JOIN_QUEUE",
                            mode = "PVP",
                            difficulty = currentDifficulty
                        )
                    )
                    playGame()
                }
                "2" -> {
                    println("Iniciando partida PVE ($currentDifficulty)...")
                    connection.sendMessage(NetworkMessage(type = "START_GAME", mode = "PVE", difficulty = currentDifficulty))
                    playGame()
                }
                "3" -> {
                    println("Solicitando records...")
                    connection.sendMessage(NetworkMessage(type = "GET_RECORDS"))
                    Thread.sleep(1000)
                }
                "4" -> showConfigMenu()
                "5" -> {
                    println("¡Hasta pronto!")
                    connection.disconnect()
                    exitProcess(0)
                }
                else -> println("Opción no válida. Inténtalo de nuevo.")
            }
        }
    }

    private fun showConfigMenu() {
        println("\n--- CONFIGURACIÓN DE DIFICULTAD ---")
        println("1. FÁCIL   (Palabras comunes)")
        println("2. MEDIO   (Palabras intermedias)")
        println("3. DIFÍCIL (Palabras complejas)")
        print("Elige una dificultad: ")

        when (readlnOrNull()?.trim()) {
            "1" -> currentDifficulty = "EASY"
            "2" -> currentDifficulty = "MEDIUM"
            "3" -> currentDifficulty = "HARD"
            else -> println("Opción no válida. Se mantiene $currentDifficulty.")
        }
        println("✅ Dificultad configurada a: $currentDifficulty")
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
                connection.sendMessage(NetworkMessage(type = "LEAVE_GAME"))
                break
            }

            if (guess.length != 5) {
                println("La palabra debe tener exactamente 5 letras.")
                continue
            }

            attempts++
            connection.sendMessage(NetworkMessage(type = "GUESS", word = guess, attempt = attempts))
            Thread.sleep(200)
        }
    }
}