package server.src.records

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

object RecordManager {
    private val file = File("src/main/resources/records.json")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private var records: MutableMap<String, PlayerStats> = mutableMapOf()

    init {
        loadRecords()
    }

    private fun loadRecords() {
        if (file.exists()) {
            try {
                val json = file.readText()
                val listType = object : TypeToken<List<PlayerStats>>() {}.type
                val list: List<PlayerStats> = gson.fromJson(json, listType) ?: emptyList()
                records = list.associateBy { it.playerName }.toMutableMap()
            } catch (e: Exception) {
                println("Error al cargar records: ${e.message}")
            }
        }
    }

    private fun saveRecords() {
        try {
            file.parentFile.mkdirs()
            val json = gson.toJson(records.values.toList())
            file.writeText(json)
        } catch (e: Exception) {
            println("Error al guardar records: ${e.message}")
        }
    }

    @Synchronized
    fun getStats(playerName: String): PlayerStats {
        return records.getOrPut(playerName) { PlayerStats(playerName) }
    }

    // 🔹 Hemos añadido 'attempts' (intentos) y 'timeInSeconds' (tiempo) con valores por defecto
    // para que no te dé error en los otros archivos mientras los actualizamos.
    @Synchronized
    fun recordWinPVE(playerName: String, attempts: Int = 1, timeInSeconds: Long = 0) {
        val stats = getStats(playerName)
        stats.gamesPlayed++
        stats.gamesWonPVE++
        stats.currentStreak++
        if (stats.currentStreak > stats.maxStreak) stats.maxStreak = stats.currentStreak

        // Guardamos en qué intento ha acertado para la "Distribución de intentos"
        val attemptString = attempts.coerceAtMost(6).toString()
        stats.guessDistribution[attemptString] = (stats.guessDistribution[attemptString] ?: 0) + 1
        stats.totalTimeSeconds += timeInSeconds

        saveRecords()
    }

    @Synchronized
    fun recordLossPVE(playerName: String, timeInSeconds: Long = 0) {
        val stats = getStats(playerName)
        stats.gamesPlayed++
        stats.gamesLost++
        stats.currentStreak = 0 // Pierde la racha
        stats.totalTimeSeconds += timeInSeconds
        saveRecords()
    }

    @Synchronized
    fun recordWinPVP(playerName: String, attempts: Int = 1, timeInSeconds: Long = 0) {
        val stats = getStats(playerName)
        stats.gamesPlayed++
        stats.gamesWonPVP++
        stats.currentStreak++
        if (stats.currentStreak > stats.maxStreak) stats.maxStreak = stats.currentStreak

        val attemptString = attempts.coerceAtMost(6).toString()
        stats.guessDistribution[attemptString] = (stats.guessDistribution[attemptString] ?: 0) + 1
        stats.totalTimeSeconds += timeInSeconds

        saveRecords()
    }

    @Synchronized
    fun recordLossPVP(playerName: String, timeInSeconds: Long = 0) {
        val stats = getStats(playerName)
        stats.gamesPlayed++
        stats.gamesLost++
        stats.currentStreak = 0
        stats.totalTimeSeconds += timeInSeconds
        saveRecords()
    }

    fun getRecordsJson(): String {
        return gson.toJson(records.values.toList())
    }
}