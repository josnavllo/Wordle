package server.src.game

import org.example.server.src.network.LetterResult
import org.example.server.src.network.NetworkMessage
import server.src.dictionary.DictionaryService
import server.src.network.ClientHandler
import server.src.records.RecordManager

class PvPGame(private val player1: ClientHandler, private val player2: ClientHandler, private val difficulty: String) {
    private val secretWord = DictionaryService.pickRandomWord("${difficulty.lowercase()}.txt", 5)
    private var isGameOver = false
    private val startTime = System.currentTimeMillis()

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

        opponent.sendMessage(NetworkMessage(type = "OPPONENT_PROGRESS", attempts = attempt))

        // 🔹 NUEVA LÓGICA WORDLE EXACTA (Doble pasada para PVP)
        val resultList = MutableList(secretWord.length) { LetterResult("", "ABSENT") }
        val charCounts = mutableMapOf<Char, Int>()
        secretWord.forEach { charCounts[it] = charCounts.getOrDefault(it, 0) + 1 }

        // Primera pasada: Verdes
        for (i in guessedWord.indices) {
            if (guessedWord[i] == secretWord[i]) {
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

        player.sendMessage(NetworkMessage(type = "GUESS_RESULT", word = guessedWord, result = resultList))

        if (guessedWord == secretWord) {
            isGameOver = true
            val timeInSeconds = (System.currentTimeMillis() - startTime) / 1000

            player.sendMessage(NetworkMessage(type = "ROUND_WINNER", word = secretWord, attempts = attempt))
            opponent.sendMessage(NetworkMessage(type = "ERROR", payload = "¡Has perdido! Tu rival adivinó la palabra: $secretWord"))

            RecordManager.recordWinPVP(player.playerName, attempt, timeInSeconds)
            RecordManager.recordLossPVP(opponent.playerName, timeInSeconds)

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
        val timeInSeconds = (System.currentTimeMillis() - startTime) / 1000
        val opponent = if (player == player1) player2 else player1

        opponent.sendMessage(NetworkMessage(type = "ERROR", payload = "Tu rival se ha desconectado. ¡Ganas por abandono!"))

        RecordManager.recordWinPVP(opponent.playerName, 1, timeInSeconds)
        RecordManager.recordLossPVP(player.playerName, timeInSeconds)

        player.currentGame = null
        opponent.currentGame = null
    }
}