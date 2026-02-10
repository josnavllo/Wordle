package org.example.client.src

import java.net.Socket

fun main() {
    val socket = Socket("localhost", 5678)

    val output = socket.getOutputStream().bufferedWriter()
    val input = socket.getInputStream().bufferedReader()

    output.write("""{"type":"HELLO","payload":"Soy el cliente"}""")
    output.newLine()
    output.flush()

    val response = input.readLine()
    println("Servidor dice: $response")

    socket.close()
}
