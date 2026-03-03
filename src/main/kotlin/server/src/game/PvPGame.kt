package server.src.game

import org.example.server.network.LetterResult
import org.example.server.network.NetworkMessage
import org.example.server.src.dictionary.DictionaryService
import server.src.network.ClientHandler
import org.example.server.src.records.RecordManager

class PvPGame(private val player1: ClientHandler, private val player2: ClientHandler, private val difficulty: String) {
    private val secretWord = DictionaryService.pickRandomWord("${difficulty.lowercase()}.txt", 5)
    private var isGameOver = false

    fun start() {
        val startMsg = NetworkMessage(type = "START_GAME", mode = "PVP", wordLength = 5, rounds = 1)
        player1.sendMessage(startMsg)
        player2.sendMessage(startMsg)

        player1.sendMessage(NetworkMessage(type = "INFO", payload = "¡Partida PVP encontrada! Adivina la palabra antes que tu rival."))
        player2.sendMessage(NetworkMessage(type = "INFO", payload = "¡Partida PVP encontrada! Adivina la palabra antes que tu rival."))
    }

    @Synchronized
    fun processGuess(player: ClientHandler, guessedWord: String, attempt: Int) {
        if (isGameOver) return

        val opponent = if (player == player1) player2 else player1

        if (guessedWord.length != secretWord.length) {
            player.sendMessage(NetworkMessage(type = "ERROR", payload = "Debe tener ${secretWord.length} letras"))
            return
        }

        // Analizamos la palabra
        val resultList = guessedWord.mapIndexed { index, c ->
            when {
                secretWord[index] == c -> LetterResult(c.toString(), "CORRECT")
                secretWord.contains(c) -> LetterResult(c.toString(), "PRESENT")
                else -> LetterResult(c.toString(), "ABSENT")
            }
        }

        player.sendMessage(NetworkMessage(type = "GUESS_RESULT", word = guessedWord, result = resultList))

        // Si acierta, gana la partida inmediatamente
        if (guessedWord == secretWord) {
            isGameOver = true
            player.sendMessage(NetworkMessage(type = "ROUND_WINNER", word = secretWord, attempts = attempt))
            opponent.sendMessage(NetworkMessage(type = "ERROR", payload = "¡Has perdido! Tu rival adivinó la palabra: $secretWord"))

            RecordManager.recordWinPVP("Global")
            RecordManager.recordLossPVP("Global")

            player.currentGame = null
            opponent.currentGame = null
        } else if (attempt >= 6) {
            player.sendMessage(NetworkMessage(type = "INFO", payload = "Te quedaste sin intentos. Esperando al rival..."))
        }
    }

    @Synchronized
    fun playerDisconnected(player: ClientHandler) {
        if (isGameOver) return
        isGameOver = true
        val opponent = if (player == player1) player2 else player1
        opponent.sendMessage(NetworkMessage(type = "ERROR", payload = "Tu rival se ha desconectado. ¡Ganas por abandono!"))

        player.currentGame = null
        opponent.currentGame = null
    }
}