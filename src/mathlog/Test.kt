package mathlog

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.StreamTokenizer
import java.util.*

fun main() {
    val reader = BufferedReader(InputStreamReader(System.`in`),131072)
    val t = generateSequence { PropositionalParser.parse(reader.readLine()) }
    for (line in t) println(line)
}