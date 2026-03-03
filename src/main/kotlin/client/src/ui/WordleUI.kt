package client.src.ui

import client.src.network.LetterResult

object WordleUI {
    private const val RESET = "\u001B[0m"
    private const val BG_GREEN = "\u001B[42m\u001B[30m"
    private const val BG_YELLOW = "\u001B[43m\u001B[30m"
    private const val BG_GRAY = "\u001B[100m\u001B[37m"

    // La cuadrícula de 6 filas y 5 columnas
    private var grid = MutableList(6) { MutableList(5) { LetterResult(" ", "EMPTY") } }
    // El estado del teclado
    private var keyboard = mutableMapOf<String, String>()

    var currentAttempt = 0
    var opponentAttempts = 0
    var isPvP = false

    fun reset(pvp: Boolean) {
        grid = MutableList(6) { MutableList(5) { LetterResult(" ", "EMPTY") } }
        keyboard.clear()
        "QWERTYUIOPASDFGHJKLZXCVBNM".forEach { keyboard[it.toString()] = "EMPTY" }
        currentAttempt = 0
        opponentAttempts = 0
        isPvP = pvp
    }

    fun addAttempt(result: List<LetterResult>) {
        if (currentAttempt < 6) {
            grid[currentAttempt] = result.toMutableList()
            currentAttempt++

            // Actualizamos el teclado: Verde manda sobre Amarillo, Amarillo manda sobre Gris.
            result.forEach {
                val currentStatus = keyboard[it.letter] ?: "EMPTY"
                if (it.status == "CORRECT") keyboard[it.letter] = "CORRECT"
                else if (it.status == "PRESENT" && currentStatus != "CORRECT") keyboard[it.letter] = "PRESENT"
                else if (it.status == "ABSENT" && currentStatus == "EMPTY") keyboard[it.letter] = "ABSENT"
            }
        }
    }

    fun draw() {
        // "Limpiamos" la consola imprimiendo saltos de línea para que parezca una pantalla fija
        println("\n".repeat(30))

        // --- PANEL DE ESTADO ---
        println("╔══════════════════════════════════════╗")
        println("║         WORDLE MULTIJUGADOR          ║")
        println("╠══════════════════════════════════════╣")
        println("║ Intentos restantes: ${6 - currentAttempt}                ║")
        if (isPvP) {
            println("║ Progreso rival: $opponentAttempts/6 intentos          ║")
        }
        println("╚══════════════════════════════════════╝\n")

        // --- CUADRÍCULA 6x5 ---
        for (row in grid) {
            var rowStr = "          " // Espaciado para centrar
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
            println() // Espacio entre filas
        }

        // --- TECLADO QWERTY ---
        println("\n        --- TECLADO ---")
        val rows = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM")
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
            // Centrado bonito del teclado
            val padding = if (r.startsWith("A")) "   " else if (r.startsWith("Z")) "      " else " "
            println("    " + padding + kStr)
            println()
        }
    }
}