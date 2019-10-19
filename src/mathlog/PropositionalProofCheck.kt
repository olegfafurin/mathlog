package mathlog

import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import kotlin.collections.HashSet
import kotlin.system.exitProcess

fun Expression.propositionalAxiom(n: Int): Boolean {
    when (n) {
        1 -> {
            if ((this is Implication) && (rhs is Implication) && (lhs == rhs.rhs)) return true
            return false
        }
        2 -> {
            if (this is Implication && rhs is Implication) {
                val firstTerm = lhs
                val midTerm = rhs.lhs
                val lastTerm = rhs.rhs
                if ((firstTerm is Implication) && (lastTerm is Implication) && (midTerm is Implication) && (midTerm.rhs is Implication)) {
                    return ((firstTerm.lhs == midTerm.lhs) && (firstTerm.lhs == lastTerm.lhs) && (firstTerm.rhs == midTerm.rhs.lhs) && (midTerm.rhs.rhs == lastTerm.rhs))
                }
            }
            return false
        }
        3 -> {
            if (this is Implication && rhs is Implication) {
                if (rhs.rhs is Conjunction && lhs == rhs.rhs.lhs && rhs.lhs == rhs.rhs.rhs) return true
            }
            return false
        }
        4 -> {
            return (this is Implication && lhs is Conjunction && lhs.lhs == rhs)
        }
        5 -> {
            return (this is Implication && lhs is Conjunction && lhs.rhs == rhs)
        }
        6 -> {
            return (this is Implication && rhs is Disjunction && lhs == rhs.lhs)
        }
        7 -> {
            return (this is Implication && rhs is Disjunction && lhs == rhs.rhs)
        }
        8 -> {
            if (this is Implication && lhs is Implication && rhs is Implication && rhs.lhs is Implication && rhs.rhs is Implication && rhs.rhs.lhs is Disjunction) {
                return ((lhs.lhs == rhs.rhs.lhs.lhs) && (rhs.lhs.lhs == rhs.rhs.lhs.rhs) && (lhs.rhs == rhs.lhs.rhs) && (lhs.rhs == rhs.rhs.rhs))
            }
        }
        9 -> {
            if (this is Implication && rhs is Implication) {
                val firstTerm = lhs
                val midTerm = rhs.lhs
                val lastTerm = rhs.rhs
                if (firstTerm is Implication && midTerm is Implication && lastTerm is Negation && midTerm.rhs is Negation) {
                    return (firstTerm.lhs == midTerm.lhs && firstTerm.lhs == lastTerm.expression && firstTerm.rhs == midTerm.rhs.expression)
                }
            }
            return false
        }
        10 -> {
            if (this is Implication && lhs is Negation && lhs.expression is Negation) return (lhs.expression.expression == rhs)
            return false
        }
        else -> {
            return false
        }
    }
    return false
}

class PropositionalProofCheck {

    private var lines: MutableList<Expression> = mutableListOf()
    private var proved: HashMap<Expression, Int> = HashMap()
    private var rightParts: HashMap<Expression, MutableSet<Int>> = HashMap()
    private lateinit var result: Expression
    private var dependencies: MutableMap<Int, Pair<Int, Int>> = mutableMapOf()
    private var resultLineNumber = 0
    private var indices = mutableSetOf<Int>()
    private var reEnum = mutableMapOf<Int, Int>()

    private lateinit var reader: BufferedReader
//    private val tokenizer = StringTokenizer("")

    private fun endWithError() {
        println("Proof is incorrect")
        exitProcess(0)
    }

//    fun getLine() = PropositionalParser.parse(readLine()?.filter { it != '\t' && it != '\r' && it != ' ' })


    private fun dfs(n: Int) {
        val a = dependencies[n]
        indices.add(n)
        if (a != null) {
            if (!indices.contains(a.first)) dfs(a.first)
            if (!indices.contains(a.second)) dfs(a.second)
        }
    }

    private fun resolveDependencies() {
        dfs(resultLineNumber)
        var c = 0
        for (i in indices.sorted()) reEnum[i] = ++c
    }

    fun start(fileName: String = "") {
        var firstLine: String
        var context: List<Expression>
        var contextHs: HashSet<Expression>
        var test: HashMap<Expression, Int>
        var isAxiom: MutableMap<Int, Int> = mutableMapOf()
        var isHypothesis: MutableMap<Int, Int> = mutableMapOf()
        var body: List<Expression>
        var l: Sequence<String>
        if (fileName == "") {
            reader = BufferedReader(InputStreamReader(System.`in`))
            firstLine = reader.readLine()!!.filter { it != '\t' && it != '\r' && it != ' ' }
            l = generateSequence { reader.readLine().filter { it != ' ' && it != '\t' && it !='\r' } }
        } else {
            reader = BufferedReader(InputStreamReader(FileInputStream(fileName)))
            firstLine = reader.readLine().filter { it != '\t' && it != '\r' && it != ' ' }
            l = generateSequence { readLine() }
            reader.close()
        }
        context = firstLine.split("|-").first().split(',').mapNotNull { PropositionalParser.parse(it) }
        contextHs = HashSet(context)
        result = PropositionalParser.parse(firstLine.split("|-")[1])!!
        var counter = 0
        var nextLine: Expression
        var beforeLast = ""
        for (line in l) {
            if (line == "") break
            beforeLast = line
            nextLine = PropositionalParser.parse(line)!!
            if (proved.contains(nextLine)) { // check whether the statement had been deduced already
                continue
            }
            if (contextHs.contains(nextLine)) { // check whether the statement is given from context
                isHypothesis[++counter] = context.indexOfFirst { it == nextLine } + 1
                lines.add(nextLine)
                proved[nextLine] = counter
                if (nextLine is Implication) {
                    if (nextLine.rhs in rightParts) rightParts[nextLine.rhs]?.add(counter)
                    else rightParts[nextLine.rhs] = mutableSetOf(counter)
                }
                if (nextLine == result && resultLineNumber == 0) resultLineNumber = counter
                continue
            }
            val testAxiom = (1..10).firstOrNull { nextLine.propositionalAxiom(it) }
            if (testAxiom != null) { // check whether the statement is an axiom
                isAxiom[++counter] = testAxiom
                lines.add(nextLine)
                proved[nextLine] = counter
                if (nextLine is Implication) {
                    if (nextLine.rhs in rightParts) rightParts[nextLine.rhs]?.add(counter)
                    else rightParts[nextLine.rhs] = mutableSetOf(counter)
                }
                if (nextLine == result && resultLineNumber == 0) resultLineNumber = counter
                continue
            }
            if (rightParts.contains(nextLine)) {
                val i = rightParts[nextLine]!!.firstOrNull { proved.contains((lines[it - 1] as Implication).lhs) }
                if (i == null) endWithError()
                lines.add(nextLine)
                proved[nextLine] = ++counter
                if (nextLine is Implication) {
                    if (nextLine.rhs in rightParts) rightParts[nextLine.rhs]?.add(counter)
                    else rightParts[nextLine.rhs] = mutableSetOf(counter)
                }
                if (nextLine == result && resultLineNumber == 0) resultLineNumber = counter
                dependencies[counter] = Pair(i!!, proved[(lines[i - 1] as Implication).lhs]!!)
                continue
            }
            endWithError()
        }
        if (beforeLast != result.print())
        if (lines[counter - 1] != result) endWithError()
        resolveDependencies()
        output { println(context.joinToString { it.print() } + " |- ${result.print()}") }
        var finalLines = mutableListOf<Expression>()
        output {
            for (i in indices.sortedWith(compareBy { reEnum[it] })) {

                when {
                    dependencies[i] != null -> println("[${reEnum[i]}. M.P. ${reEnum[dependencies[i]!!.first]}, ${reEnum[dependencies[i]!!.second]}] ${lines[i - 1].print()}")
                    isAxiom[i] != null -> println("[${reEnum[i]}. Ax. sch. ${isAxiom[i]}] ${lines[i - 1].print()}")
                    isHypothesis[i] != null -> println("[${reEnum[i]}. Hypothesis ${isHypothesis[i]}] ${lines[i - 1].print()}")
                    else -> println("ERROR")
                }

            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.isNotEmpty()) {
                PropositionalProofCheck().start(args[0])
            } else PropositionalProofCheck().start()
        }

    }
}