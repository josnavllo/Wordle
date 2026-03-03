package server.src.records

data class PlayerStats(
    val playerName: String,
    var gamesPlayed: Int = 0,
    var gamesWonPVE: Int = 0,
    var gamesWonPVP: Int = 0,
    var gamesLost: Int = 0,
    var currentStreak: Int = 0,
    var maxStreak: Int = 0,

    var guessDistribution: MutableMap<String, Int> = mutableMapOf(
        "1" to 0, "2" to 0, "3" to 0, "4" to 0, "5" to 0, "6" to 0
    ),

    var totalTimeSeconds: Long = 0
) {

    val winPercentage: Double
        get() = if (gamesPlayed > 0) ((gamesWonPVE + gamesWonPVP).toDouble() / gamesPlayed) * 100.0 else 0.0

    val averageTimeSeconds: Double
        get() = if (gamesPlayed > 0) totalTimeSeconds.toDouble() / gamesPlayed else 0.0
}