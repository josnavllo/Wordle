package org.example.server.network

data class NetworkMessage(
    val type: MessageType,
    val payload: String = ""
)