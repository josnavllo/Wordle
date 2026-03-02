package org.example.server.src.records

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object RecordManager {
    // Guardaremos el archivo en la raíz del proyecto para fácil acceso
    private val file = File("records.json")
    private val gson = Gson()
    private var stats: MutableList<PlayerStats> = mutableListOf()

    init {
        load()
    }

    @Synchronized
    private fun load() {
        if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<MutableList<PlayerStats>>() {}.type
            stats = gson.fromJson(json, type) ?: mutableListOf()
        } else {
            // Si no existe, creamos un jugador global por defecto
            stats = mutableListOf(PlayerStats(playerName = "Global"))
            save()
        }
    }

    @Synchronized
    private fun save() {
        file.writeText(gson.toJson(stats))
    }

    @Synchronized
    fun recordWinPVE(playerName: String = "Global") {
        val player = getOrCreatePlayer(playerName)
        player.gamesPlayedPVE++
        player.gamesWonPVE++
        player.currentStreak++
        if (player.currentStreak > player.maxStreak) {
            player.maxStreak = player.currentStreak
        }
        save()
    }

    @Synchronized
    fun recordLossPVE(playerName: String = "Global") {
        val player = getOrCreatePlayer(playerName)
        player.gamesPlayedPVE++
        player.currentStreak = 0 // Pierde la racha
        save()
    }

    // Devuelve un String JSON formateado para enviarlo al cliente
    fun getRecordsJson(): String {
        return gson.toJson(stats)
    }

    private fun getOrCreatePlayer(name: String): PlayerStats {
        var player = stats.find { it.playerName == name }
        if (player == null) {
            player = PlayerStats(playerName = name)
            stats.add(player)
        }
        return player
    }
}