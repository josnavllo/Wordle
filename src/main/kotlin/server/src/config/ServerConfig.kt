package org.example.server.src.config

import java.util.Properties

class ServerConfig private constructor(
    val host: String,
    val port: Int,
    val maxClients: Int
) {

    companion object {

        fun load(): ServerConfig {

            val props = Properties()

            val input = ServerConfig::class.java
                .getResourceAsStream("/server.properties")
                ?: throw RuntimeException("No se encontró server.properties en resources")

            props.load(input)

            return ServerConfig(
                host = props.getProperty("server.host"),
                port = props.getProperty("server.port").toInt(),
                maxClients = props.getProperty("max.clients").toInt()
            )
        }
    }
}