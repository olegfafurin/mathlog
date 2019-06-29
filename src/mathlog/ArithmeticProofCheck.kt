package mathlog

import kotlin.system.exitProcess

/**
 * Created by imd on 28/06/2019
 */
var n = 0

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
        11 -> {
            if (this is Implication && lhs is All) {
                val phi = lhs.expression
                val varName = lhs.variable
                TODO() // 1st  axiom of predicate calculus
            }
        }
        12 -> {
            TODO() // 2nd axiom of predicate calculus
        }
        else -> {
            return false
        }
    }
    return false
}

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

private fun Expression.getFreeVariables(): Set<String> {
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


private fun Expression.substitute(name: String, part: Expression): Expression? {
    if (this is ArithmeticVariable) return if (this.name == name) part else this
    if (this is Add) return Add(lhs.substitute(name, part) as ArithmeticExpression, rhs.substitute(name, part) as ArithmeticExpression)
    if (this is Mult) return Mult(lhs.substitute(name, part) as ArithmeticExpression, rhs.substitute(name, part) as ArithmeticExpression)
    if (this is Equals) return Equals(lhs.substitute(name, part) as ArithmeticExpression, rhs.substitute(name, part) as ArithmeticExpression)
    if (this is All) {
        return if (variable.name == name) null
        else All(variable, expression.substitute(name, part)!!)
    }
    if (this is Exist) {
        return if (variable.name == name) null
        else Exist(variable, expression.substitute(name, part)!!)
    }
    if (this is Negation) return Negation(expression.substitute(name, part)!!)
    if (this is Conjunction) return Conjunction(lhs.substitute(name, part)!!, rhs.substitute(name, part)!!)
    if (this is Disjunction) return Disjunction(lhs.substitute(name, part)!!, rhs.substitute(name, part)!!)
    if (this is Implication) return Implication(lhs.substitute(name, part)!!, rhs.substitute(name, part)!!)
    if (this is Stroke) return Stroke(expr.substitute(name, part) as ArithmeticExpression)
    if (this is Zero) return this
    if (this is Predicate) return Predicate(name, terms.map { it.substitute(name, part) as ArithmeticExpression })
    if (this is Function) return Function(name, terms.map { it.substitute(name, part) as ArithmeticExpression })
    return null
}

private fun Expression.arithmeticAxiom(n: Int): Boolean {
    if (n in 1..8) {
        return ArithmeticParser.parse(arithmeticAxioms[n]) == this
    } else if (n == 9) {
        if (this is Implication && lhs is Conjunction && lhs.rhs is All && lhs.rhs.expression is Implication && lhs.rhs.expression.lhs == rhs) {
            return rhs.substitute("x", Zero()) == lhs.lhs && lhs.rhs.expression.rhs == rhs.substitute("x", Stroke(ArithmeticVariable("x")))
        }
    }
    return false
}

fun main() {
    val firstLine = readLine()!!.filter { it != '\t' && it != '\r' && it != ' ' }
    val context = firstLine.split("|-").first().split(',').filter { it != "" }.map { ArithmeticParser.parse(it) }
    val r = firstLine.split("|-")
    val result = ArithmeticParser.parse(firstLine.split("|-")[1])!!
    val body = generateSequence { ArithmeticParser.parse(readLine()?.filter { it != '\t' && it != '\r' && it != ' ' }) }
    println("CONTEXT:")
    for (t in context) println(t!!.print())
    println("RESULT")
    println(result.print())
    for (e in body) {
        n++
        println(e.print())
        println(e.getFreeVariables().joinToString(separator = ","))
        if ((1..9).firstOrNull { e.arithmeticAxiom(it) } != null) continue
        if ((1..10).firstOrNull {e.satisfy(it)} != null ) continue

    }
}

fun endWithError() {
    println("Line #$n cannot be obtained")
    exitProcess(0)
}
