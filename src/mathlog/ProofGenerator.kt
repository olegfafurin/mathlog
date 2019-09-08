package mathlog

import kotlin.math.exp
import kotlin.math.max
import kotlin.system.exitProcess

/**
 * Created by imd on 01/09/2019
 */

//  proof.txt is required

val swap1 = ArithmeticParser.parse("a=b->a=b->b=a")!!
val TRUTH = ArithmeticParser.parse("0=0->0=0->0=0")!!
val EQ = ArithmeticParser.parse("@a.a=a")!!
val COMM = ArithmeticParser.parse("@a.@b(a=b->a=a->b=a)")!!
val TOSTROKE = ArithmeticParser.parse("@a.@b.(a=b->a'=b')")!!
val TRANS = fromFile("proof.txt")

val AX = mapOf(
        1 to "a=b->a'=b'",
        2 to "a=b->a=c->b=c",
        3 to "a'=b'->a=b",
        4 to "!a'=0",
        5 to "a+b'=(a+b)'",
        6 to "a+0=a",
        7 to "a*0=0",
        8 to "a*b'=a*b+a"
)

fun substitute(expr: Expression, varName: String, part: Expression): Expression {
    if (expr is All && expr.variable.name == varName) {
        val desired = expr.expression.substitute(varName, part)!!
        println(Implication(expr, desired).print())
        println(desired.print())
        return desired
    }
    return expr
}

fun swapEq(lhs: ArithmeticExpression, rhs: ArithmeticExpression) { // considering lhs = rhs, proves rhs == lhs
    val self = substitute(EQ, "a", lhs)
    val impl1 = substitute(COMM, "a", lhs)
    val impl2 = substitute(impl1, "b", rhs)
    // considering lhs == rhs is already proved
    println((impl2 as Implication).rhs.print())
    println(Equals(rhs, lhs).print())
}

fun generalize(expr: Expression): Expression {
    val s = expr.getFreeVariables().intersect(listOf("c", "b", "a")).toList().sortedDescending()
    println(Implication(expr, Implication(TRUTH, expr)).print())
    println(Implication(TRUTH, expr).print())
    var current = expr
    for (varName in s) {
        current = All(ArithmeticVariable(varName), current)
        println(Implication(TRUTH, current).print())
    }
    println(current.print())
    return current
}

fun z(n: Int): ArithmeticExpression {
    var c: ArithmeticExpression = Zero()
    for (i in 1..n) c = Stroke(c)
    return c
}

fun applyAxiom(a: Expression = ArithmeticVariable("a"), b: Expression = ArithmeticVariable("b"), c: Expression = ArithmeticVariable("c"), n: Int) {

}

fun step(x: Int) { // we know that 0''*0^x=0^2x
    val toX = z(x)
    val to2X = z(2 * x)
    val to2 = z(2)
    swapEq(Mult(to2, toX), to2X) // 0^2x = 2 * 0^x
    stroke(to2X, Mult(to2, toX)) // (0 ^ 2x)' = (2 * 0^x)'
    stroke(Stroke(to2X), Stroke(Mult(to2, toX))) // (0 ^ 2x)'' = (2 * 0^x)''
    genAxiom(8, mapOf("a" to to2, "b" to toX))
//    println(Equals(Mult(to2, Stroke(toX)), Add(Mult(to2, toX), to2)).print()) // 2 * 0^(x + 1) = 2*0^x + 2
    swapEq(Mult(to2, Stroke(toX)), Add(Mult(to2, toX), to2)) // 2*0^x + 2 = 2 * 0^(x + 1)
    genAxiom(5, mapOf("a" to Mult(to2, toX), "b" to Stroke(Zero())))
//    println(Equals(Add(Mult(to2, toX), to2), Stroke(Add(Mult(to2, toX), Stroke(Zero())))).print()) // 2 * 0^x + 2 = (2*0^x + 1)'

    transEq(Add(Mult(to2, toX), to2), Stroke(Add(Mult(to2, toX), Stroke(Zero()))), Mult(to2, Stroke(toX))) // (2*0^x + 1)' = 2 * 0^(x + 1)
    genAxiom(5, mapOf("a" to Mult(to2, toX), "b" to Zero()))
//    println(Equals(Add(Mult(to2, toX), Stroke(Zero())), Stroke(Add(Mult(to2, toX), Zero()))).print()) // 2*0^x + 1 = (2*0*x + 0)'
    stroke(Add(Mult(to2, toX), Stroke(Zero())), Stroke(Add(Mult(to2, toX), Zero()))) // (2*0^x + 1)' = (2*0*x + 0)''
    transEq(Stroke(Add(Mult(to2, toX), Stroke(Zero()))), Stroke(Stroke(Add(Mult(to2, toX), Zero()))), Mult(to2, Stroke(toX))) //  (2*0^x + 0)'' = 2*0^(x+1)
    genAxiom(6, mapOf("a" to Mult(to2, toX)))
//    println(Equals(Add(Mult(to2, toX), Zero()), Mult(to2, toX)).print()) // 2*0^x + 0 = 2 * 0^x
    stroke(Add(Mult(to2, toX), Zero()), Mult(to2, toX)) // (2*0^x + 0)' = (2 * 0^x)'
    stroke(Stroke(Add(Mult(to2, toX), Zero())), Stroke(Mult(to2, toX))) // (2*0^x + 0)'' = (2 * 0^x)''
    transEq(Stroke(Stroke(Add(Mult(to2, toX), Zero()))), Stroke(Stroke(Mult(to2, toX))), Mult(to2, Stroke(toX))) // (2 * 0^x)'' = 2*0^(x+1)
    println(Equals(Mult(to2, toX), to2X).print()) // 2 * 0^x = 0^2x -- already written?!
    stroke(Mult(to2, toX), to2X) // (2 * 0^x)' = (0^2x)'
    stroke(Stroke(Mult(to2, toX)), Stroke(to2X)) // (2 * 0^x)'' = (0^2x)''
    transEq(Stroke(Stroke(Mult(to2, toX))), Mult(to2, Stroke(toX)), Stroke(Stroke(to2X))) // 2*0^(x+1) = 0^2x+2

//    println()
}

fun genAxiom(n: Int, m: Map<String, Expression>) {
    var ax = generalize(ArithmeticParser.parse(AX[n])!!)
//            ArithmeticParser.parse(AX[n])!!
    for (e in m) {
        ax = substitute(ax, e.key, e.value)
//        println(ax.print())
    }
}

fun transEq(a: ArithmeticExpression, b: ArithmeticExpression, c: ArithmeticExpression) { // considering a = b; a = c proves b = c
    genAxiom(2, mapOf("a" to a, "b" to b, "c" to c))
//    println(Implication(Equals(a, b), Implication(Equals(a, c), Equals(b, c))).print())
    // a = b is already present
    println(Implication(Equals(a, c), Equals(b, c)).print())
    // a = c is already present
    println(Equals(b, c).print())
}

fun stroke(a: ArithmeticExpression, b: ArithmeticExpression) {
    val t = Equals(Stroke(a), Stroke(b))
//    println(t.print())
    genAxiom(1, mapOf("a" to a, "b" to b))
//    val s = substitute(TOSTROKE, "a", a)
//    val e = substitute(s, "b", b)
//    println(s.print())
//    println(e.print())
//    println(t.print())

    // a = b is already present
    println(t.print())

//    println(Implication(t, Equals(Stroke(a), Stroke(b))).print()) // a = b -> a' = b'
//    println(Equals(Stroke(a), Stroke(b)).print()) // a' = b'
}

fun main() {
    val (a, b) = readLine()!!.split(" ").map { it.toInt() }
    var s: ArithmeticExpression = Zero()
    for (i in 1..a) s = Stroke(s)
    var t: ArithmeticExpression = Zero()
    for (i in 1..b) t = Stroke(t)
    t = Mult(Stroke(Stroke(Zero())), t)
    for (line in TRANS) println(line.print())
//    println("###########")
    for (ax in AX.values.map { ArithmeticParser.parse(it)!! }) println(ax.print())
    for (ax in AX.mapValues { generalize(ArithmeticParser.parse(it.value)!!) }.values) {
        println(ax.print())
    }
//    println("_____")
    if (a % 2 == 0 && b % 2 == 0) {
        println(Implication(All(ArithmeticVariable("a"), Equals(Mult(ArithmeticVariable("a"), Zero()), Zero())), Equals(Mult(Stroke(Stroke(Zero())), Zero()), Zero())).print())
        println(Equals(Mult(Stroke(Stroke(Zero())), Zero()), Zero()).print()) // 0'' * 0 = 0
        for (i in 0 until max(a / 2, b / 2)) step(i)
        val toa2 = z(a / 2)
        val tob2 = z(b / 2)
        val toA = z(a)
        val toB = z(b)
        val exprA = Equals(Mult(Stroke(Stroke(Zero())), toa2), toA)
        val exprB = Equals(Mult(Stroke(Stroke(Zero())), tob2), toB)
        println(Implication(exprA, Implication(exprB, Conjunction(exprA, exprB))).print())
        println(Implication(exprB, Conjunction(exprA, exprB)).print())
        println(Conjunction(exprA, exprB).print())
        val irrA = Equals(Mult(Stroke(Stroke(Zero())), toa2), Add(toA, Stroke(Zero())))
        val irrB = Equals(Mult(Stroke(Stroke(Zero())), tob2), Add(toB, Stroke(Zero())))
        println(Implication(Conjunction(exprA, exprB), Disjunction(Conjunction(exprA, exprB), Conjunction(irrA, irrB))).print())
        val total = Disjunction(Conjunction(exprA, exprB), Conjunction(irrA, irrB))
        println(total.print())
        println(Implication(total, Exist(ArithmeticVariable("q"), Disjunction(Conjunction(exprA, Equals(Mult(z(2), ArithmeticVariable("q")), toB)), Conjunction(irrA, Equals(Mult(z(2), ArithmeticVariable("q")), Add(toB, z(1))))))).print())
        println(Exist(ArithmeticVariable("q"), Disjunction(Conjunction(exprA, Equals(Mult(z(2), ArithmeticVariable("q")), toB)), Conjunction(irrA, Equals(Mult(z(2), ArithmeticVariable("q")), Add(toB, z(1)))))).print())
        val impl = Exist(ArithmeticVariable("q"), Disjunction(Conjunction(exprA, Equals(Mult(z(2), ArithmeticVariable("q")), toB)), Conjunction(irrA, Equals(Mult(z(2), ArithmeticVariable("q")), Add(toB, z(1))))))
        val conclusion = Exist(ArithmeticVariable("p"), Exist(ArithmeticVariable("q"), Disjunction(Conjunction(Equals(Mult(z(2), ArithmeticVariable("p")), toA), Equals(Mult(z(2), ArithmeticVariable("q")), toB)), Conjunction(Equals(Mult(z(2), ArithmeticVariable("p")), Add(toA, z(1))), Equals(Mult(z(2), ArithmeticVariable("q")), Add(toB, z(1)))))))
        println(Implication(impl, conclusion).print())
        println(conclusion.print())
        exitProcess(0)
    }
    if (a % 2 == 1 && b % 2 == 1) {
        println(Implication(All(ArithmeticVariable("a"), Equals(Mult(ArithmeticVariable("a"), Zero()), Zero())), Equals(Mult(Stroke(Stroke(Zero())), Zero()), Zero())).print())
        println(Equals(Mult(Stroke(Stroke(Zero())), Zero()), Zero()).print()) // 0'' * 0 = 0
        for (i in 0 until max((a + 1) / 2, (b + 1) / 2)) step(i)
        println("_______________")
        val toA = z(a)
        val toB = z(b)
        processOdd(a)
        processOdd(b)
        val exprA = Equals(Mult(z(2), z((a + 1) / 2)), Add(z(a), z(1)))
        val exprB = Equals(Mult(z(2), z((b + 1) / 2)), Add(z(b), z(1)))
        val irrA = Equals(Mult(z(2), z((a + 1) / 2)), z(a))
        val irrB = Equals(Mult(z(2), z((b + 1) / 2)), z(b))
        println(Implication(exprA, Implication(exprB, Conjunction(exprA, exprB))).print())
        println(Implication(exprB, Conjunction(exprA, exprB)).print())
        println(Conjunction(exprA, exprB).print())
        println(Implication(Conjunction(exprA, exprB), Disjunction(Conjunction(irrA, irrB), Conjunction(exprA, exprB))).print())
        val total = Disjunction(Conjunction(irrA, irrB), Conjunction(exprA, exprB))
        println(total.print())
        println(Implication(total, Exist(ArithmeticVariable("q"), Disjunction(Conjunction(irrA, Equals(Mult(z(2), ArithmeticVariable("q")), toB)), Conjunction(exprA, Equals(Mult(z(2), ArithmeticVariable("q")), Add(toB, z(1))))))).print())
        println(Exist(ArithmeticVariable("q"), Disjunction(Conjunction(irrA, Equals(Mult(z(2), ArithmeticVariable("q")), toB)), Conjunction(exprA, Equals(Mult(z(2), ArithmeticVariable("q")), Add(toB, z(1)))))).print())
        val impl = Exist(ArithmeticVariable("q"), Disjunction(Conjunction(irrA, Equals(Mult(z(2), ArithmeticVariable("q")), toB)), Conjunction(exprA, Equals(Mult(z(2), ArithmeticVariable("q")), Add(toB, z(1))))))
        val conclusion = Exist(ArithmeticVariable("p"), Exist(ArithmeticVariable("q"), Disjunction(Conjunction(Equals(Mult(z(2), ArithmeticVariable("p")), toA), Equals(Mult(z(2), ArithmeticVariable("q")), toB)), Conjunction(Equals(Mult(z(2), ArithmeticVariable("p")), Add(toA, z(1))), Equals(Mult(z(2), ArithmeticVariable("q")), Add(toB, z(1)))))))
        println(Implication(impl, conclusion).print())
        println(conclusion.print())
        exitProcess(0)
    }
}

fun processOdd(n: Int) {
    assert(n % 2 == 1) // only for odd numbers
    val toN = z(n)
    genAxiom(5, mapOf("a" to toN, "b" to Zero())) // n + 1 = (n + 0)'
    swapEq(Add(toN, Stroke(Zero())), Stroke(Add(toN, Zero()))) // (n + 0)' = n + 1
    genAxiom(6, mapOf("a" to toN)) // n + 0 = n
    stroke(Add(toN, Zero()), toN) // (n + 0)' = n'
    transEq(Stroke(Add(toN, Zero())), Stroke(toN), Add(toN, Stroke(Zero()))) // n' = n + 1
    // 2 * (n+1)/2 = (n + 1) is already written
    val ton12 = z((n + 1) / 2)
    swapEq(Mult(Stroke(Stroke(Zero())), ton12), Stroke(toN)) // (n+1) = 2 * (n+1)/2
    transEq(Stroke(toN), Mult(Stroke(Stroke(Zero())), ton12), Add(toN, Stroke(Zero()))) // 2 * ((n+1)/2) = n + 1
}
