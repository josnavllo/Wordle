package org.example.server.network

data class NetworkMessage(
    val type: String,
    val payload: String? = null,
    val mode: String? = null,
    val wordLength: Int? = null,
    val rounds: Int? = null,
    val word: String? = null,
    val attempt: Int? = null,
    val result: List<LetterResult>? = null,
    val player: String? = null,
    val attempts: Int? = null
)

data class LetterResult(
    val letter: String,
    val status: String
)