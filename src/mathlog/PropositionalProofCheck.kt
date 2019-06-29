package mathlog

import kotlin.system.exitProcess

class PropositionalProofCheck {

    private var lines: MutableList<Expression> = mutableListOf()
    private lateinit var result: Expression
    private var dependencies: MutableMap<Int, Pair<Int, Int>> = mutableMapOf()
    private var resultLineNumber = 0
    private var indicies = mutableListOf<Int>()
    private var reEnum = mutableMapOf<Int, Int>()

    private fun Expression.satisfy(n: Int): Boolean {
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

    private fun endWithError() {
        println("Proof is incorrect")
        exitProcess(0)
    }

//    fun getLine() = PropositionalParser.parse(readLine()?.filter { it != '\t' && it != '\r' && it != ' ' })


    private fun dfs(n: Int) {
        val a = dependencies[n]
        indicies.add(n)
        if (a != null) {
            dfs(a.first)
            dfs(a.second)
        }
    }

    private fun resolveDependencies() {
        dfs(resultLineNumber)
        var c = 0
        for (i in indicies.sorted()) reEnum[i] = ++c
    }

    fun start() {
        val firstLine = readLine()!!.filter { it != '\t' && it != '\r' && it != ' ' }
        val context = firstLine.split("|-").first().split(',').map { PropositionalParser.parse(it) }
        result = PropositionalParser.parse(firstLine.split("|-")[1])!!
//        answer = mutableListOf(context.joinToString { it?.print() ?: "" } + " |- ${result.print()}")
//        nextLine = getLine()
        val isAxiom: MutableMap<Int, Int> = mutableMapOf()
        val isHypothesis: MutableMap<Int, Int> = mutableMapOf()
        val body = generateSequence { PropositionalParser.parse(readLine()?.filter { it != '\t' && it != '\r' && it != ' ' }) }
        val l = body.toList()
        var counter = 0
        for (nextLine in l) {
            if (lines.any { it == nextLine }) { // check whether the statement had been deduced already
//                nextLine = getLine()
                continue
            }
            if (context.indexOfFirst { it == nextLine } != -1) { // check whether the statement is given from context
//                answer.add("[${++counter}. Hypothesis ${context.indexOfFirst { it == nextLine } + 1}] ${nextLine.print()}")
                isHypothesis[++counter] = context.indexOfFirst { it == nextLine } + 1
                lines.add(nextLine)
                if (nextLine == result && resultLineNumber == 0) resultLineNumber = counter
//                nextLine = getLine()
                continue
            }
            if ((1..10).firstOrNull { nextLine.satisfy(it) } != null) { // check whether the statement is an axiom
//                answer.add("[${++counter}. Ax. sch. ${(1 until 10).first { nextLine.satisfy(it) }}] ${nextLine.print()}")
                isAxiom[++counter] = (1..10).first { nextLine.satisfy(it) }
                lines.add(nextLine)
                if (nextLine == result && resultLineNumber == 0) resultLineNumber = counter
//                nextLine = getLine()
                continue
            }
            val candidates = lines.filter { it is Implication && it.rhs == nextLine }
            val match =
                    candidates.firstOrNull { candidate -> lines.any { it == (candidate as Implication).lhs } } // check whether the statement is modus ponens
            if (match == null) endWithError()
            else {
                val matchingMP = lines.indexOfFirst { it == match } + 1
                val assumption = lines.indexOfFirst { it == (match as Implication).lhs } + 1
//                answer.add("[${++counter}. M.P. $matchingMP, $assumption] ${nextLine.print()}")
                dependencies[++counter] = Pair(matchingMP, assumption)
                lines.add(nextLine)
            }
            if (nextLine == result) resultLineNumber = counter
        }
        if (l.last() != result) endWithError()
        resolveDependencies()
        println(context.joinToString { it?.print() ?: "" } + " |- ${result.print()}")
        for (i in indicies.sortedWith(compareBy { reEnum[it] })) {
            when {
                dependencies[i] != null -> println("[${reEnum[i]}. M.P. ${reEnum[dependencies[i]!!.first]}, ${reEnum[dependencies[i]!!.second]}] ${lines[i - 1].print()}")
                isAxiom[i] != null -> println("[${reEnum[i]}. Ax. sch. ${isAxiom[i]}] ${lines[i - 1].print()}")
                isHypothesis[i] != null -> println("[${reEnum[i]}. Hypothesis ${isHypothesis[i]}] ${lines[i - 1].print()}")
                else -> println("ERROR")
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            PropositionalProofCheck().start()
        }
    }
}