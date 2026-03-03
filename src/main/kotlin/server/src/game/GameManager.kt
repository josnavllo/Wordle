package org.example.server.src.game

import org.example.server.src.network.ClientHandler
import java.util.concurrent.ConcurrentLinkedQueue

object GameManager {
    // Tres colas separadas por dificultad
    private val queues = mapOf(
        "EASY" to ConcurrentLinkedQueue<ClientHandler>(),
        "MEDIUM" to ConcurrentLinkedQueue<ClientHandler>(),
        "HARD" to ConcurrentLinkedQueue<ClientHandler>()
    )

    fun joinQueue(client: ClientHandler, difficulty: String) {
        // Usamos EASY por defecto si mandan algo raro
        val queue = queues[difficulty] ?: queues["EASY"]!!

        if (!queue.contains(client)) {
            queue.add(client)
            checkQueue(difficulty)
        }
    }

    fun leaveQueue(client: ClientHandler) {
        queues.values.forEach { it.remove(client) }
    }

    @Synchronized
    private fun checkQueue(difficulty: String) {
        val queue = queues[difficulty]!!
        if (queue.size >= 2) {
            val player1 = queue.poll()
            val player2 = queue.poll()

            if (player1 != null && player2 != null) {
                val game = PvPGame(player1, player2, difficulty)
                player1.currentGame = game
                player2.currentGame = game
                game.start()
            }
        }
    }
}