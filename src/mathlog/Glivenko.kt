package mathlog

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.lang.IllegalStateException
import java.security.AccessControlContext

/**
 * Created by imd on 01/09/2019
 */

private val ATOA = listOf(
        "(A->(A->A->A))->(A->(A->A->A)->A)->(A->A)",
        "A->A->A",
        "(A->A->A)->A->(A->A->A)",
        "A->(A->A->A)",
        "A->(A->A->A)->A",
        "(A->(A->A->A)->A)->(A->A)",
        "A->A"
).map { PropositionalParser.parse(it)!! }

//private val ATONOTNOTA = fromFile("AtoNotNotA.in")
private val NOTNOTA = fromFile("notnotA.in")
private val CONTR = fromFile("contrap_full.in")
private val IMPL = fromFile("int_impl.in")
private val TENTH = fromFile("tenth2.in")

fun Expression.subst(corr: Map<Variable, Expression>): Expression? {
    if (this is Negation) return Negation(expression.subst(corr)!!)
    if (this is Conjunction) return Conjunction(lhs.subst(corr)!!, rhs.subst(corr)!!)
    if (this is Disjunction) return Disjunction(lhs.subst(corr)!!, rhs.subst(corr)!!)
    if (this is Implication) return Implication(lhs.subst(corr)!!, rhs.subst(corr)!!)
    if (this is Variable) {
        return if (this in corr.keys) corr[this] else this
    }
    return null
}

fun fromFile(fileName: String): List<Expression> {
    return BufferedReader(InputStreamReader(FileInputStream(fileName))).readLines().drop(1).map { ArithmeticParser.parse(it.filter { char -> char != ' ' }) }.filterNotNull()
}

fun deduct(proof: List<Expression>, context: List<Expression>, assumption: Expression) {
    var proved = mutableSetOf<Expression>()
    for (line in proof) {
        var n = (1..10).firstOrNull { line.propositionalAxiom(it) }
        if (n != null || line in context) {
            println(line.print())
            println(Implication(line, Implication(assumption, line)).print())
            println(Implication(assumption, line).print())
            proved.add(line)
        } else if (line != assumption) {
            val candidates = proved.filter { it is Implication && it.rhs == line }
            val match =
                    candidates.firstOrNull { candidate -> proved.contains((candidate as Implication).lhs) }!! // check whether the statement is modus ponens
            val firstPart = Implication(assumption, (match as Implication).lhs)
            val secondPart = Implication(assumption, match)
            val thirdPart = Implication(assumption, line)
            println(Implication(firstPart, Implication(secondPart, thirdPart)).print())
            println(Implication(secondPart, thirdPart).print())
            println(thirdPart.print())
            proved.add(line)
        } else {
            val p = substitute(ATOA, mapOf(Variable("A") to assumption))
            proved.add(line)
        }
    }
}

fun process(line: String) {

}

class Output {
    val outputSb = StringBuilder()
    fun print(o: Any?) {
        outputSb.append(o)
    }

    fun println() {
        outputSb.append('\n')
    }
}

inline fun output(block: Output.() -> Unit) {
    val o = Output().apply(block); print(o.outputSb)
}

fun substitute(proof: List<Expression>, corr: Map<Variable, Expression>) {
    output {
        for (line in proof) {
            println(line.subst(corr)!!.print())
        }
    }
}

class Glivenko {
    fun start() {
        val firstLine = readLine()!!.filter { it != '\t' && it != '\r' && it != ' ' }
//        val start = System.currentTimeMillis()
        val context = firstLine.split("|-").first().split(",").filterNot { it.isEmpty() }.map { PropositionalParser.parse(it)!! }.toSet()
        val result = PropositionalParser.parse(firstLine.split("|-")[1])!!
        val proof = generateSequence { PropositionalParser.parse(readLine()?.filter { it != '\t' && it != '\r' && it != ' ' }) }.toList()
        var proved = mutableSetOf<Expression>()
        output { println(context.joinToString { it.print() } + " |- ${Negation(Negation(result)).print()}") }
//        for (line in context) {
//        println(line.print())
//            substitute(NOTNOTA, mapOf("A" to line.print()))
//        println(Implication(line, Negation(Negation(line))).print())
//        println(Negation(Negation(line)).print())
//            proved.add(line)
//        }
        for (line in proof) {
            val n = (1..9).firstOrNull { line.propositionalAxiom(it) }
            if (n != null) {
                substitute(NOTNOTA, mapOf(Variable("A") to line))
                proved.add(line)
                continue
            }
            if (context.contains(line)) {
                substitute(NOTNOTA, mapOf(Variable("A") to line))
                proved.add(line)
                continue
            }
            if (line.propositionalAxiom(10)) {
                val g = (line as Implication).rhs
//                val ng = Negation(g)
//                val nng = Negation(Negation(g))
//                println(Implication(g, Implication(nng, g)).print())
//                substitute(CONTR, mapOf("A" to g.print(), "B" to Implication(nng, g).print()))
//                println(Implication(ng, Implication(nng, g)).print()) // tenth intuitionistic axiom is applied here
//                substitute(CONTR, mapOf("A" to ng.print(), "B" to Implication(nng, g).print()))
//                val firstPart = Implication(Negation(Implication(nng, g)), ng)
//                val secondPart = Implication(Negation(Implication(nng, g)), nng)
//                val thirdPart = Negation(Negation(Implication(nng, g)))
//                println(Implication(firstPart, Implication(secondPart, thirdPart)).print())
//                println(Implication(secondPart, thirdPart).print())
//                println(thirdPart.print())
                substitute(TENTH, mapOf(Variable("A") to g))
                proved.add(line)
                continue
            }
            val candidates = proved.filter { it is Implication && it.rhs == line }
            val match = candidates.firstOrNull { candidate -> proved.contains((candidate as Implication).lhs) }
            if (match != null) {
                substitute(IMPL, mapOf(Variable("G") to (match as Implication).lhs, Variable("H") to match.rhs))
                proved.add(line)
                continue
            } else {
//                println("ERROR! MP NOT FOUND FOR ${line.print()}")
//                throw IllegalStateException("No matching MP found")
//                Thread.sleep(30000)
            }
        }
//        println("${System.currentTimeMillis() - start} ms elapsed")
//        println("${proved.size} proved")

//    deduct(proof, Variable("A"))
//    substitute(ATOA, mapOf("A" to Negation(Variable("A"))))
//    println(PropositionalParser.parse("A->A->A")!!.print())
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Glivenko().start()
        }
    }
}