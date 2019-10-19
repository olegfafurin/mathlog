package mathlog

import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import kotlin.system.exitProcess

/**
 * Created by imd on 28/06/2019
 */
private var n = 0

fun Expression.substitute(name: String, part: Expression, forb: Set<String> = setOf()): Expression? {
    if (this is ArithmeticVariable) {
        if (this.name in forb) return null
        return if (this.name == name) part else this
    }
    if (this is Add) return Add(lhs.substitute(name, part, forb) as ArithmeticExpression, rhs.substitute(name, part, forb) as ArithmeticExpression)
    if (this is Mult) return Mult(lhs.substitute(name, part, forb) as ArithmeticExpression, rhs.substitute(name, part, forb) as ArithmeticExpression)
    if (this is Equals) return Equals(lhs.substitute(name, part, forb) as ArithmeticExpression, rhs.substitute(name, part, forb) as ArithmeticExpression)
    if (this is All) {
        return when {
            variable.name == name -> this
            variable.name in part.getFreeVariables() -> All(variable, expression.substitute(name, part, forb.plus(name))!!)
            else -> All(variable, expression.substitute(name, part, forb)!!)
        }
    }
    if (this is Exist) {
        return when {
            variable.name == name -> this
            variable.name in part.getFreeVariables() -> Exist(variable, expression.substitute(name, part, forb.plus(name))!!)
            else -> Exist(variable, expression.substitute(name, part, forb)!!)
        }
    }
    if (this is Negation) return Negation(expression.substitute(name, part, forb)!!)
    if (this is Conjunction) return Conjunction(lhs.substitute(name, part, forb)!!, rhs.substitute(name, part, forb)!!)
    if (this is Disjunction) return Disjunction(lhs.substitute(name, part, forb)!!, rhs.substitute(name, part, forb)!!)
    if (this is Implication) return Implication(lhs.substitute(name, part, forb)!!, rhs.substitute(name, part, forb)!!)
    if (this is Stroke) return Stroke(expr.substitute(name, part, forb) as ArithmeticExpression)
    if (this is Zero) return this
    if (this is Predicate) return Predicate(this.name, terms.map { it.substitute(name, part, forb) as ArithmeticExpression })
    if (this is Function) return Function(this.name, terms.map { it.substitute(name, part, forb) as ArithmeticExpression })
    return null
}

fun Expression.getFreeVariables(): Set<String> {
    if (this is BinaryOperation) return lhs.getFreeVariables().plus(rhs.getFreeVariables())
    if (this is All) return expression.getFreeVariables().filter { it != variable.name }.toSet()
    if (this is Exist) return expression.getFreeVariables().filter { it != variable.name }.toSet()
    if (this is Stroke) return expr.getFreeVariables()
    if (this is Function) {
        var current = mutableSetOf<String>()
        for (t in terms) current.addAll(t.getFreeVariables())
        return current
    }
    if (this is Predicate) {
        var current = mutableSetOf<String>()
        for (t in terms) current.addAll(t.getFreeVariables())
        return current
    }
    if (this is Negation) return expression.getFreeVariables()
    if (this is ArithmeticVariable) return setOf(this.name)
    if (this is Zero) return emptySet()
    return emptySet()
}

fun Expression.satisfy(n: Int): Boolean {
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
        11 -> {
            if (this is Implication && lhs is All) {
                val phi = lhs.expression
                val varName = lhs.variable.name
                try {
                    val candidate = discover(rhs, phi, varName)
                    if (phi.substitute(varName, candidate?:ArithmeticVariable(varName)) == rhs) return true
                } // 1st  axiom of predicate calculus
                catch (e: Exception) {
		// Not 11th
                }
            }
            return false
        }
        12 -> {
            if (this is Implication && rhs is Exist) {
                val varName = rhs.variable.name
                val phi = rhs.expression
                try {
                    val candidate = discover(lhs, phi, varName)
                    if (phi.substitute(varName, candidate?:ArithmeticVariable(varName)) == lhs) return true
                } catch (e: Exception) {
		// Not 12th
                }
            }
            return false
        }
        else -> {
            return false
        }
    }
    return false
}

fun discover(expr: Expression, pattern: Expression, name: String): Expression? {
    if (pattern is ArithmeticVariable) {
        if (pattern.name == name) return expr
        else if (expr !is ArithmeticVariable) throw Exception()
        if (expr.name == pattern.name) return null // pattern?
        else throw Exception()
    }
    if (pattern.javaClass.name != expr.javaClass.name) throw Exception()
    if (expr is BinaryOperation && pattern is BinaryOperation) return discover(expr.lhs, pattern.lhs, name)
            ?: discover(expr.rhs, pattern.rhs, name)
    if (expr is Stroke && pattern is Stroke) return discover(expr.expr, pattern.expr, name)
    if (expr is Function && pattern is Function) {
        if (expr.name != pattern.name || expr.terms.size != pattern.terms.size) throw Exception()
        for (i in expr.terms.indices) {
            val candidate = discover(expr.terms[i], pattern.terms[i], name)
            if (candidate != null) return candidate
        }
        return null
    }
    if (expr is Predicate && pattern is Predicate) {
        if (expr.name != pattern.name || expr.terms.size != pattern.terms.size) throw Exception()
        for (i in expr.terms.indices) {
            val candidate = discover(expr.terms[i], pattern.terms[i], name)
            if (candidate != null) return candidate
        }
        return null
    }
    if (expr is Zero && pattern is Zero) return null
    if (expr is Negation && pattern is Negation) return discover(expr.expression, pattern.expression, name)
    if (expr is All && pattern is All) {
        if (expr.variable.name == pattern.variable.name && pattern.variable.name != name) return discover(expr.expression, pattern.expression, name)
        if (expr == pattern) return null
        throw Exception()
    }
    if (expr is Exist && pattern is Exist) {
        if (expr.variable.name == pattern.variable.name && pattern.variable.name != name) return discover(expr.expression, pattern.expression, name)
        if (expr == pattern) return null
        throw Exception()
    }
    throw Exception()
}

class ArithmeticProofCheck {

    private var lines: HashSet<Expression> = HashSet()
    private lateinit var reader: BufferedReader



    private val arithmeticAxioms = mapOf(
            1 to "a=b->a'=b'",
            2 to "a=b->a=c->b=c",
            3 to "a'=b'->a=b",
            4 to "!a'=0",
            5 to "a+b'=(a+b)'",
            6 to "a+0=a",
            7 to "a*0=0",
            8 to "a*b'=a*b+a"
    )

    private fun Expression.arithmeticAxiom(n: Int): Boolean {
        if (n in 1..8) {
            return ArithmeticParser.parse(arithmeticAxioms[n]) == this
        } else if (n == 9) {
            if (this is Implication && lhs is Conjunction && lhs.rhs is All && lhs.rhs.expression is Implication && lhs.rhs.expression.lhs == rhs) {
                val varName = lhs.rhs.variable.name
                return rhs.substitute(varName, Zero()) == lhs.lhs && lhs.rhs.expression.rhs == rhs.substitute(varName, Stroke(ArithmeticVariable(varName)))
            }
        }
        return false
    }

    fun start(filename: String = "") {
        reader = if (filename != "") BufferedReader(InputStreamReader(FileInputStream(filename)))
        else BufferedReader(InputStreamReader(System.`in`))
        val firstLine = reader.readLine()!!.filter { it != '\t' && it != '\r' && it != ' ' }
        val contextLine = firstLine.split("|-").first()
        val contextParser = ArithmeticParser(contextLine)
        val context = generateSequence { contextParser.start() }.toSet()
        val r = firstLine.split("|-")
        val result = ArithmeticParser.parse(firstLine.split("|-")[1])!!
        val restrictedVariables: MutableSet<String> = mutableSetOf()
        val body = generateSequence {
            try {
                ArithmeticParser.parse(reader.readLine()?.filter { it != '\t' && it != '\r' && it != ' ' })
            }
            catch (e: Exception) {
                e.printStackTrace()
                println("line #$n")
                null
            }
        }
        println("CONTEXT:")
        for (t in context) println(t.print())
        println("RESULT")
        println(result.print())
        for (contextExpression in context) restrictedVariables.addAll(contextExpression.getFreeVariables());
        for (expression in body) {
            n++
            if ((1..9).firstOrNull { expression.arithmeticAxiom(it) } != null) {
                lines.add(expression)
                continue
            }
            if ((1..12).firstOrNull { expression.satisfy(it) } != null) {
                lines.add(expression)
                continue
            }
            if (context.contains(expression)) {
                lines.add(expression)
                continue
            }
            val candidatesMP = lines.filter { it is Implication && it.rhs == expression }
            if (candidatesMP.firstOrNull { candidate -> lines.contains((candidate as Implication).lhs) } != null) { // check whether the statement is modus ponens
                lines.add(expression)
                continue
            }
            var phi: Expression
            var psi: Expression
            var varName: String

            if (expression is Implication && expression.rhs is All) {
                    phi = expression.lhs
                    psi = expression.rhs.expression
                    varName = expression.rhs.variable.name
                    if (varName !in phi.getFreeVariables() && varName !in restrictedVariables && lines.contains(Implication(phi, psi))) {
                        lines.add(expression)
                        continue
                    }
            }
            if (expression is Implication && expression.lhs is Exist) {
                varName = expression.lhs.variable.name
                phi = expression.rhs
                psi = expression.lhs.expression
                if (varName !in phi.getFreeVariables() && varName !in restrictedVariables && lines.contains(Implication(psi, phi))) {
                    lines.add(expression)
                    continue
                }
            }

            endWithError()
        }
        if (result !in lines ) {
            println("Required hasn't been proven")
            exitProcess(0)
        }
        println("Proof is correct")
    }

    private fun endWithError() {
        println("Line #$n cannot be obtained")
        exitProcess(0)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.isNotEmpty()) ArithmeticProofCheck().start(args[0])
            else ArithmeticProofCheck().start()
        }
    }
}