package mathlog

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.StreamTokenizer
import java.util.*

fun main() {

//    val plus1 = fromFile("gen${File.separator}plus1_impl_not.in")
//    val last = ArithmeticParser.parse("@r.((!((0''*p)=r'))->(!((0''*p)=(r+0'))))")!!

//    val proof = fromFile("gen\\tenth.in", true)
//    substitute(CONTR, mapOf(Variable("A") to Conjunction(Variable("A"), Variable("B")), Variable("B") to Variable("B")))
//    substitute(fromFile("gen${File.separator}notand.in", true), mapOf(Variable("A") to Equals(Zero(), Zero()), Variable("B") to Equals(z(1), z(2))))
//    substitute(fromFile("gen\\tenth.in", propos = true), mapOf(Variable("A") to Negation(Equals(z(1),z(2))),Variable("B") to Equals(Zero(), Zero())))
//    deduct(proof, listOf(), Negation(Predicate("A", listOf())))
//    for (line in fromFile("gen${File.separator}plus1_impl_not.in")) println(line.print())
    val (a, b) = readLine()!!.split(" ").map { it.toInt() }
    println("@p.@q.(0''*p=0${"\'".repeat(a)}&0''*q=0${"\'".repeat(b)})|(0''*p=0${"\'".repeat(a)}+0'&0''q=0${"\'".repeat(b)}+0')")
    val phi = ArithmeticParser.parse("@p.@q.(0''*p=0${"\'".repeat(a)}&0''*q=0${"\'".repeat(b)})|(0''*p=0${"\'".repeat(a)}+0'&0''*q=0${"\'".repeat(b)}+0')")!!
    println(phi.print())
}