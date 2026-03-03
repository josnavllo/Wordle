package server.src.records

data class PlayerStats(
    val playerName: String = "Jugador",
    var gamesPlayedPVE: Int = 0,
    var gamesWonPVE: Int = 0,
    var gamesPlayedPVP: Int = 0,
    var gamesWonPVP: Int = 0,
    var currentStreak: Int = 0,
    var maxStreak: Int = 0
)