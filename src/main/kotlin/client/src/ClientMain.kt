package client.src

import client.src.network.ServerConnection
import client.src.ui.MainMenu
import client.src.network.NetworkMessage

fun main() {
    println("=== BIENVENIDO A WORDLE MULTIJUGADOR ===")
    print("Introduce tu nombre de jugador: ")

    val nombre = readlnOrNull()?.trim()?.takeIf { it.isNotEmpty() } ?: "Anónimo"

    println("Conectando al servidor...")
    val connection = ServerConnection("localhost", 5678)

    try {
        connection.connect()

        connection.sendMessage(NetworkMessage(type = "HELLO", player = nombre))

        // Arrancamos el menú principal
        val menu = MainMenu(connection)
        menu.show()

    } catch (e: Exception) {
        println("No se pudo conectar al servidor. Asegúrate de que está encendido.")
        println("Error: ${e.message}")
    }
}