package org.example.server.src.dictionary

object DictionaryService {
    fun pickRandomWord(filename: String, length: Int): String {
        val stream = javaClass.getResourceAsStream("/dictionary/$filename")
            ?: throw RuntimeException("No se encontró $filename en resources/dictionary")
        val words = stream.bufferedReader().readLines().filter { it.length == length }
        if (words.isEmpty()) throw RuntimeException("No hay palabras de longitud $length")
        return words.random().uppercase()
    }
}