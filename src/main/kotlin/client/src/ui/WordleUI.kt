package client.src.ui

import client.src.network.LetterResult

object WordleUI {
    private const val RESET = "\u001B[0m"
    private const val BG_GREEN = "\u001B[42m\u001B[30m"
    private const val BG_YELLOW = "\u001B[43m\u001B[30m"
    private const val BG_GRAY = "\u001B[100m\u001B[37m"

    private var grid = MutableList(6) { MutableList(5) { LetterResult(" ", "EMPTY") } }
    private var keyboard = mutableMapOf<String, String>()

    var currentAttempt = 0
    var opponentAttempts = 0
    var isPvP = false


    private var startTime: Long = 0

    fun reset(pvp: Boolean) {
        grid = MutableList(6) { MutableList(5) { LetterResult(" ", "EMPTY") } }
        keyboard.clear()
        "QWERTYUIOPASDFGHJKLÑZXCVBNM".forEach { keyboard[it.toString()] = "EMPTY" }
        currentAttempt = 0
        opponentAttempts = 0
        isPvP = pvp
        // 🔹 NUEVO: Iniciamos el reloj
        startTime = System.currentTimeMillis()
    }

    fun addAttempt(result: List<LetterResult>) {
        if (currentAttempt < 6) {
            grid[currentAttempt] = result.toMutableList()
            currentAttempt++

            result.forEach {
                val currentStatus = keyboard[it.letter] ?: "EMPTY"
                if (it.status == "CORRECT") keyboard[it.letter] = "CORRECT"
                else if (it.status == "PRESENT" && currentStatus != "CORRECT") keyboard[it.letter] = "PRESENT"
                else if (it.status == "ABSENT" && currentStatus == "EMPTY") keyboard[it.letter] = "ABSENT"
            }
        }
    }

    fun draw() {
        println("\n".repeat(30))

        // 🔹 NUEVO: Calculamos el tiempo formateado en MM:SS
        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)

        println("╔══════════════════════════════════════╗")
        println("║         WORDLE MULTIJUGADOR          ║")
        println("╠══════════════════════════════════════╣")
        println("║ Intentos restantes: ${6 - currentAttempt}                ║")
        // 🔹 Añadimos el tiempo al panel de estado
        println("║ Tiempo transcurrido: $timeString           ║")
        if (isPvP) {
            println("║ Progreso rival: $opponentAttempts/6 intentos          ║")
        }
        println("╚══════════════════════════════════════╝\n")

        for (row in grid) {
            var rowStr = "          "
            for (cell in row) {
                val color = when(cell.status) {
                    "CORRECT" -> BG_GREEN
                    "PRESENT" -> BG_YELLOW
                    "ABSENT" -> BG_GRAY
                    else -> ""
                }
                rowStr += if (cell.status == "EMPTY") "[   ] " else "$color[ ${cell.letter} ]$RESET "
            }
            println(rowStr)
            println()
        }

        println("\n        --- TECLADO ---")
        val rows = listOf("QWERTYUIOP", "ASDFGHJKLÑ", "ZXCVBNM")
        for (r in rows) {
            var kStr = ""
            for (char in r) {
                val status = keyboard[char.toString()]
                val color = when(status) {
                    "CORRECT" -> BG_GREEN
                    "PRESENT" -> BG_YELLOW
                    "ABSENT" -> BG_GRAY
                    else -> ""
                }
                kStr += if (status == "EMPTY") " $char " else "$color $char $RESET"
            }
            val padding = if (r.startsWith("A")) "  " else if (r.startsWith("Z")) "       " else " "
            println("    " + padding + kStr)
            println()
        }
    }
}