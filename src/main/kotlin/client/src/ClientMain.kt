package org.example.client.src

import org.example.client.src.network.NetworkMessage
import org.example.client.src.network.ServerConnection
import client.src.ui.MainMenu

fun main() {
    println("Conectando al servidor...")
    val connection = ServerConnection("localhost", 5678)

    try {
        connection.connect()
        // Mandamos un saludo inicial (opcional)
        connection.sendMessage(NetworkMessage(type = "HELLO", payload = "Soy el cliente"))

        // Arrancamos el menú principal
        val menu = MainMenu(connection)
        menu.show()

    } catch (e: Exception) {
        println("No se pudo conectar al servidor. Asegúrate de que está encendido.")
        println("Error: ${e.message}")
    }
}