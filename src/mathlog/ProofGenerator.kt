package mathlog

import java.io.File
import kotlin.math.exp
import kotlin.math.max
import kotlin.system.exitProcess

/**
 * Created by imd on 01/09/2019
 */

//  proof.txt is required

val TRUTH = ArithmeticParser.parse("0=0->0=0->0=0")!!
val EQ = ArithmeticParser.parse("@a.a=a")!!
val COMM = ArithmeticParser.parse("@a.@b(a=b->a=a->b=a)")!!
val TOSTROKE = ArithmeticParser.parse("@a.@b.(a=b->a'=b')")!!
val CONTR = fromFile("contrap_full.in", true)
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

class ProofGenerator() {
    fun substitute(expr: Expression, varName: String, part: Expression, silent: Boolean = false, container: MutableList<Expression> = mutableListOf()): Expression {
        if (expr is All && expr.variable.name == varName) {
            val desired = expr.expression.substitute(varName, part)!!
            if (!silent) {
                println(Implication(expr, desired).print())
                println(desired.print())
            }
            container.add(Implication(expr, desired))
            container.add(desired)
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

    fun generalize(expr: Expression, silent: Boolean = false, container: MutableList<Expression> = mutableListOf()): Expression {
        val s = expr.getFreeVariables().intersect(listOf("c", "b", "a")).toList().sortedDescending()
        if (!silent) {
            println(Implication(expr, Implication(TRUTH, expr)).print())
            println(Implication(TRUTH, expr).print())
        }
        container.add(Implication(expr, Implication(TRUTH, expr)))
        container.add(Implication(TRUTH, expr))
        var current = expr
        for (varName in s) {
            current = All(ArithmeticVariable(varName), current)
            if (!silent) println(Implication(TRUTH, current).print())
            container.add(Implication(TRUTH, current))
        }
        if (!silent) println(current.print())
        container.add(current)
        return current
    }

    fun z(n: Int): ArithmeticExpression {
        var c: ArithmeticExpression = Zero()
        for (i in 1..n) c = Stroke(c)
        return c
    }

    fun step(x: Int) { // we know that 0''*0^x=0^2x
        val toX = z(x)
        val to2X = z(2 * x)
        val to2 = z(2)
        swapEq(Mult(to2, toX), to2X) // 0^2x = 2 * 0^x
        stroke(to2X, Mult(to2, toX)) // (0 ^ 2x)' = (2 * 0^x)'
        stroke(Stroke(to2X), Stroke(Mult(to2, toX))) // (0 ^ 2x)'' = (2 * 0^x)''
        genAxiom(8, mapOf("a" to to2, "b" to toX))
        swapEq(Mult(to2, Stroke(toX)), Add(Mult(to2, toX), to2)) // 2*0^x + 2 = 2 * 0^(x + 1)
        genAxiom(5, mapOf("a" to Mult(to2, toX), "b" to Stroke(Zero())))
        transEq(Add(Mult(to2, toX), to2), Stroke(Add(Mult(to2, toX), Stroke(Zero()))), Mult(to2, Stroke(toX))) // (2*0^x + 1)' = 2 * 0^(x + 1)
        genAxiom(5, mapOf("a" to Mult(to2, toX), "b" to Zero()))
        stroke(Add(Mult(to2, toX), Stroke(Zero())), Stroke(Add(Mult(to2, toX), Zero()))) // (2*0^x + 1)' = (2*0*x + 0)''
        transEq(Stroke(Add(Mult(to2, toX), Stroke(Zero()))), Stroke(Stroke(Add(Mult(to2, toX), Zero()))), Mult(to2, Stroke(toX))) //  (2*0^x + 0)'' = 2*0^(x+1)
        genAxiom(6, mapOf("a" to Mult(to2, toX)))
        stroke(Add(Mult(to2, toX), Zero()), Mult(to2, toX)) // (2*0^x + 0)' = (2 * 0^x)'
        stroke(Stroke(Add(Mult(to2, toX), Zero())), Stroke(Mult(to2, toX))) // (2*0^x + 0)'' = (2 * 0^x)''
        transEq(Stroke(Stroke(Add(Mult(to2, toX), Zero()))), Stroke(Stroke(Mult(to2, toX))), Mult(to2, Stroke(toX))) // (2 * 0^x)'' = 2*0^(x+1)
        println(Equals(Mult(to2, toX), to2X).print()) // 2 * 0^x = 0^2x -- already written?!
        stroke(Mult(to2, toX), to2X) // (2 * 0^x)' = (0^2x)'
        stroke(Stroke(Mult(to2, toX)), Stroke(to2X)) // (2 * 0^x)'' = (0^2x)''
        transEq(Stroke(Stroke(Mult(to2, toX))), Mult(to2, Stroke(toX)), Stroke(Stroke(to2X))) // 2*0^(x+1) = 0^2x+2
    }

    fun genAxiom(n: Int, m: Map<String, Expression>, silent: Boolean = false, container: MutableList<Expression> = mutableListOf()) {
        var ax = generalize(ArithmeticParser.parse(AX[n])!!, silent, container)
        for (e in m) {
            ax = substitute(ax, e.key, e.value, silent, container)
        }
    }

    fun transEq(a: ArithmeticExpression, b: ArithmeticExpression, c: ArithmeticExpression, silent: Boolean = false, container: MutableList<Expression> = mutableListOf()) { // considering a = b; a = c proves b = c
        genAxiom(2, mapOf("a" to a, "b" to b, "c" to c), silent, container)
        // a = b is already present
        if (!silent) {
            println(Implication(Equals(a, c), Equals(b, c)).print())
            println(Equals(b, c).print())
        }
        container.add(Implication(Equals(a, c), Equals(b, c)))
        container.add(Equals(b, c))
        // a = c is already present
    }

    fun stroke(a: ArithmeticExpression, b: ArithmeticExpression, silent: Boolean = false, container: MutableList<Expression> = mutableListOf()) {
        val t = Equals(Stroke(a), Stroke(b))
        genAxiom(1, mapOf("a" to a, "b" to b), silent, container)
        // a = b is already present
        if (!silent) println(t.print())
        container.add(t)
    }

    fun start() {
        val (a, b) = readLine()!!.split(" ").map { it.toInt() }
        if (max(a, b) >= 100) {
            println("Numbers are too big, try the smaller ones (<100)")
            exitProcess(0)
        }
        if ((a % 2 == 0 && b % 2 == 0) || (a % 2 == 1 && b % 2 == 1)) println("|-?p.?q.(0''*p=0${"\'".repeat(a)}&0''*q=0${"\'".repeat(b)})|(0''*p=0${"\'".repeat(a)}+0'&0''*q=0${"\'".repeat(b)}+0')")
        else println("|-!(?p.?q.(0''*p=0${"\'".repeat(a)}&0''*q=0${"\'".repeat(b)})|(0''*p=0${"\'".repeat(a)}+0'&0''*q=0${"\'".repeat(b)}+0'))")
        var s: ArithmeticExpression = Zero()
        for (i in 1..a) s = Stroke(s)
        var t: ArithmeticExpression = Zero()
        for (i in 1..b) t = Stroke(t)
        t = Mult(Stroke(Stroke(Zero())), t)
        for (line in TRANS) println(line.print())
        for (ax in AX.values.map { ArithmeticParser.parse(it)!! }) println(ax.print())
        for (ax in AX.mapValues { generalize(ArithmeticParser.parse(it.value)!!) }.values) {
            println(ax.print())
        }
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
        val HEAD = fromFile("gen${File.separator}head.in")
        for (line in HEAD) println(line.print()) // proved  !2*0=1, generalized axioms, swap and a=a
        for (i in 1..101) if (i % 2 == 1) gen2ZNot(i) // !2*0 = i for all odd i's
        for (line in fromFile("gen${File.separator}2pnot1.in")) println(line.print()) // !2p=1
        for (line in fromFile("contrap_full.in")) println(line.print())
        for (i in 1..100) if (i % 2 == 1) stepNot(i) // !2p = i + 2
        for (line in fromFile("gen${File.separator}plus1_impl_not.in")) println(line.print())
        for (i in 1..101) if (i % 2 == 1) {
            val notP = Negation(Equals(Mult(z(2), ArithmeticVariable("p")), z(i)))
            println(Implication(notP, Implication(TRUTH, notP)).print())
            println(Implication(TRUTH, notP).print())
            println(Implication(TRUTH, All(ArithmeticVariable("p"), notP)).print())
            println(All(ArithmeticVariable("p"), notP).print())
            val notQ = Negation(Equals(Mult(z(2), ArithmeticVariable("q")), z(i)))
            println(Implication(All(ArithmeticVariable("p"), notP), notQ).print())
            println(notQ.print())
            println(Implication(notQ, Implication(TRUTH, notQ)).print())
            println(Implication(TRUTH, notQ).print())
            println(Implication(TRUTH, All(ArithmeticVariable("q"), notQ)).print())
            println(All(ArithmeticVariable("q"), notQ).print())

        }
        val last = ArithmeticParser.parse("@p.@r.((!((0''*p)=r'))->(!((0''*p)=(r+0'))))")!!
        if (a % 2 == 0 && b % 2 == 1) {
            val r = substitute(last, "p", ArithmeticVariable("p"))
            substitute(r, "r", z(a))
            val exprA = Equals(Mult(z(2), ArithmeticVariable("p")), Add(z(a), z(1)))
            println(Negation(exprA).print()) // !2p = a + 1
            val n2q = Negation(Equals(Mult(z(2), ArithmeticVariable("p")), z(b)))
            println(Implication(n2q, Implication(TRUTH, n2q)).print())
            println(Implication(TRUTH, n2q).print())
            println(Implication(TRUTH, All(ArithmeticVariable("p"), n2q)).print())
            println(All(ArithmeticVariable("p"), n2q).print())
            println(Implication(All(ArithmeticVariable("p"), n2q), Negation(Equals(Mult(z(2), ArithmeticVariable("q")), z(b)))).print())
            println(Negation(Equals(Mult(z(2), ArithmeticVariable("q")), z(b))).print()) // !2q = b
            val exprB = Equals(Mult(z(2), ArithmeticVariable("q")), z(b))
            val irrA = Equals(Mult(z(2), ArithmeticVariable("p")), z(a))
            substitute(fromFile("gen${File.separator}notand.in", true), mapOf(Variable("A") to irrA, Variable("B") to exprB)) // !2q = b -> !(2p = a & 2q = b)
            println(Negation(Conjunction(irrA, exprB)).print()) // !(2p = a & 2q = b)
            val irrB = Equals(Mult(z(2), ArithmeticVariable("q")), Add(z(b), z(1)))
            substitute(fromFile("gen${File.separator}notand.in", true), mapOf(Variable("A") to exprA, Variable("B") to irrB)) // !2p = a + 1 -> !(2p = a + 1 & 2q = b + 1)
            println(Negation(Conjunction(exprA, irrB)).print()) // !(2p = a + 1 & 2q = b + 1)
            substitute(fromFile("gen${File.separator}notor.in", true), mapOf(Variable("A") to Conjunction(irrA, exprB), Variable("B") to Conjunction(exprA, irrB)))  // !((2p = a & 2q = b) | (2p = a + 1 & 2q = b + 1)) // !phi
        }
        if (a % 2 == 1 && b % 2 == 0) {
            val r = substitute(last, "p", ArithmeticVariable("q"))
            substitute(r, "r", z(b))
            val exprB = Equals(Mult(z(2), ArithmeticVariable("q")), Add(z(b), z(1)))
            val irrA = Equals(Mult(z(2), ArithmeticVariable("p")), Add(z(a), z(1)))
            println(Negation(exprB).print()) // !2q = b + 1
            // !2p = a is already deduced
            val exprA = Equals(Mult(z(2), ArithmeticVariable("p")), z(a))
            val irrB = Equals(Mult(z(2), ArithmeticVariable("q")), z(b))
            substitute(fromFile("gen${File.separator}notand.in", true), mapOf(Variable("A") to exprA, Variable("B") to irrB)) // !2p=a -> !(2p=a & 2q=b)
            println(Negation(Conjunction(exprA, irrB)).print())
            substitute(fromFile("gen${File.separator}notand.in", true), mapOf(Variable("A") to irrA, Variable("B") to exprB)) // !2q= b + 1 -> !(2p=a+1 & 2q=b+1)
            println(Negation(Conjunction(irrA, exprB)).print()) // !(2p = a + 1 & 2q = b + 1)
            substitute(fromFile("gen${File.separator}notor.in", true), mapOf(Variable("A") to Conjunction(exprA, irrB), Variable("B") to Conjunction(irrA, exprB)))
        }
        val phi = ArithmeticParser.parse("(0''*p=0${"\'".repeat(a)}&0''*q=0${"\'".repeat(b)})|(0''*p=0${"\'".repeat(a)}+0'&0''*q=0${"\'".repeat(b)}+0')")!!
        println(Implication(Negation(phi), Implication(All(ArithmeticVariable("p"), All(ArithmeticVariable("q"), Negation(phi))), Negation(phi))).print()) //
        println(Implication(All(ArithmeticVariable("p"), All(ArithmeticVariable("q"), Negation(phi))), Negation(phi)).print()) // @p.@q.!phi -> !phi
        substitute(CONTR, mapOf(Variable("A") to All(ArithmeticVariable("p"), All(ArithmeticVariable("q"), Negation(phi))), Variable("B") to Negation(phi)))
        println(Implication(Negation(Negation(phi)), Negation(All(ArithmeticVariable("p"), All(ArithmeticVariable("q"), Negation(phi))))).print()) // !!phi -> !@p.@q.!phi
        val temp = Implication(Negation(Negation(phi)), Negation(All(ArithmeticVariable("p"), All(ArithmeticVariable("q"), Negation(phi)))))
        println(Implication(temp, Implication(phi, temp)).print())
        println(Implication(phi, temp).print()) // phi -> (!!phi -> !@p.@q.!phi)
        substitute(fromFile("AtoNotNotA.in", true), mapOf(Variable("A") to phi)) // phi -> !! phi
        println(Implication(Implication(phi, Negation(Negation(phi))), Implication(Implication(phi, temp), Implication(phi, Negation(All(ArithmeticVariable("p"), All(ArithmeticVariable("q"), Negation(phi))))))).print())
        println(Implication(Implication(phi, temp), Implication(phi, Negation(All(ArithmeticVariable("p"), All(ArithmeticVariable("q"), Negation(phi)))))).print())
        println(Implication(phi, Negation(All(ArithmeticVariable("p"), All(ArithmeticVariable("q"), Negation(phi))))).print()) // phi -> !@p.@q.!phi
        val anyPQ = All(ArithmeticVariable("p"), All(ArithmeticVariable("q"), Negation(phi)))
        println(Implication(Exist(ArithmeticVariable("q"), phi), Negation(anyPQ)).print())
        println(Implication(Exist(ArithmeticVariable("p"), Exist(ArithmeticVariable("q"), phi)), Negation(anyPQ)).print()) // ?p.?q.phi->!@p.@q.!phi
        substitute(CONTR, mapOf(Variable("A") to Exist(ArithmeticVariable("p"), Exist(ArithmeticVariable("q"), phi)), Variable("B") to Negation(anyPQ)))
        println(Implication(Negation(Negation(anyPQ)), Negation(Exist(ArithmeticVariable("p"), Exist(ArithmeticVariable("q"), phi)))).print()) // !!@p.@q.!phi -> !?p.?q.phi
        val impl = Implication(Negation(Negation(anyPQ)), Negation(Exist(ArithmeticVariable("p"), Exist(ArithmeticVariable("q"), phi))))
        println(Implication(impl, Implication(anyPQ, impl)).print())
        println(Implication(anyPQ, impl).print())
        substitute(fromFile("AtoNotNotA.in", true), mapOf(Variable("A") to anyPQ)) // @p.@q.!phi -> !!@p.@q.!phi
        val firstPart = Implication(anyPQ, Negation(Negation(anyPQ)))
        val midPart = Implication(anyPQ, impl)
        val lastPart = Implication(anyPQ, Negation(Exist(ArithmeticVariable("p"), Exist(ArithmeticVariable("q"), phi))))
        println(Implication(firstPart, Implication(midPart, lastPart)).print())
        println(Implication(midPart, lastPart).print())
        println(lastPart.print())

        println(Implication(Negation(phi), Implication(TRUTH, Negation(phi))).print())
        println(Implication(TRUTH, Negation(phi)).print())
        println(Implication(TRUTH, All(ArithmeticVariable("q"), Negation(phi))).print())
        println(Implication(TRUTH, All(ArithmeticVariable("p"), All(ArithmeticVariable("q"), Negation(phi)))).print())
        println(All(ArithmeticVariable("p"), All(ArithmeticVariable("q"), Negation(phi))).print())

        println(Negation(Exist(ArithmeticVariable("p"), Exist(ArithmeticVariable("q"), phi))).print())


    }

    fun processOdd(n: Int) {
        assert(n % 2 == 1) // only for odd numbers
        val toN = z(n)
        genAxiom(5, mapOf("a" to toN, "b" to Zero())) // n + 1 = (n + 0)'
        swapEq(Add(toN, Stroke(Zero())), Stroke(Add(toN, Zero()))) // (n + 0)' = n + 1
        genAxiom(6, mapOf("a" to toN)) // n + 0 = n
        stroke(Add(toN, Zero()), toN) // (n + 0)' = n'
        transEq(Stroke(Add(toN, Zero())), Stroke(toN), Add(toN, Stroke(Zero()))) // n' = n + 1
        // 2 * (n+1)/2 = (n + 1) is already deduced
        val ton12 = z((n + 1) / 2)
        swapEq(Mult(Stroke(Stroke(Zero())), ton12), Stroke(toN)) // (n+1) = 2 * (n+1)/2
        transEq(Stroke(toN), Mult(Stroke(Stroke(Zero())), ton12), Add(toN, Stroke(Zero()))) // 2 * ((n+1)/2) = n + 1
    }

    fun gen(n: Int) {
        var proof = mutableListOf<Expression>()
        proof.add(Equals(Mult(z(2), Stroke(ArithmeticVariable("p"))), z(n + 2)))
        genAxiom(8, mapOf("a" to z(2), "b" to ArithmeticVariable("p")), true, proof) // 2p' = 2p + 2
        transEq(Mult(z(2), Stroke(ArithmeticVariable("p"))), Add(Mult(z(2), ArithmeticVariable("p")), z(2)), z(n + 2), true, proof) // //2p + 2 = n + 2
        genAxiom(5, mapOf("a" to Mult(z(2), ArithmeticVariable("p")), "b" to z(1)), true, proof) // (2p + 2) = (2p + 1)'
        transEq(Add(Mult(z(2), ArithmeticVariable("p")), z(2)), Stroke(Add(Mult(z(2), ArithmeticVariable("p")), z(1))), z(n + 2), true, proof) // (2p + 1)' = n + 2
        genAxiom(3, mapOf("a" to Add(Mult(z(2), ArithmeticVariable("p")), z(1)), "b" to z(n + 1)), true, proof)
        proof.add(Equals(Add(Mult(z(2), ArithmeticVariable("p")), z(1)), z(n + 1)))
        genAxiom(5, mapOf("a" to Mult(z(2), ArithmeticVariable("p")), "b" to Zero()), true, proof) // 2p + 1 = (2p + 0)'
        transEq(Add(Mult(z(2), ArithmeticVariable("p")), z(1)), Stroke(Add(Mult(z(2), ArithmeticVariable("p")), Zero())), z(n + 1), true, proof) // (2p + 0)' = n + 1
        genAxiom(3, mapOf("a" to Add(Mult(z(2), ArithmeticVariable("p")), Zero()), "b" to z(n)), true, proof)
        proof.add(Equals(Add(Mult(z(2), ArithmeticVariable("p")), Zero()), z(n)))
        genAxiom(6, mapOf("a" to Mult(z(2), ArithmeticVariable("p"))), true, proof) // 2p + 0 = 2p
        transEq(Add(Mult(z(2), ArithmeticVariable("p")), Zero()), Mult(z(2), ArithmeticVariable("p")), z(n), true, proof) // 2p = n
        val m = n
        deduct(proof, listOf(), Equals(Mult(z(2), Stroke(ArithmeticVariable("p"))), z(m + 2))) // 2p'=n+2 -> 2p = n
    }

    fun stepNot(k: Int) {
        gen(k) // 2p'=k+2 -> 2p = k
        substitute(CONTR, mapOf(Variable("A") to Equals(Mult(z(2), Stroke(ArithmeticVariable("p"))), z(k + 2)), Variable("B") to Equals(Mult(z(2), ArithmeticVariable("p")), z(k)))) // (2p'=k+2 -> 2p = k) -> (!2p = k -> !2p' = k + 2)
        println(Implication(Negation(Equals(Mult(z(2), ArithmeticVariable("p")), z(k))), Negation(Equals(Mult(z(2), Stroke(ArithmeticVariable("p"))), z(k + 2)))).print()) // (!2p = k -> !2p' = k + 2)
        println(Negation(Equals(Mult(z(2), Stroke(ArithmeticVariable("p"))), z(k + 2))).print()) // !2p'=k + 2
        val next = Negation(Equals(Mult(z(2), Stroke(ArithmeticVariable("p"))), z(k + 2)))
        println(Implication(next, Implication(Negation(Equals(Mult(z(2), ArithmeticVariable("p")), z(k + 2))), next)).print())
        println(Implication(Negation(Equals(Mult(z(2), ArithmeticVariable("p")), z(k + 2))), next).print()) // !2p=k+2->!2p'=k+2
        val transition = Implication(Negation(Equals(Mult(z(2), ArithmeticVariable("p")), z(k + 2))), next)
        println(Implication(transition, Implication(TRUTH, transition)).print())
        println(Implication(TRUTH, transition).print())
        println(Implication(TRUTH, All(ArithmeticVariable("p"), transition)).print()) // TRUTH -> @p.(!2p=k+2->!2p'=k+2)
        println(All(ArithmeticVariable("p"), transition).print()) // @p.(!2p=k+2->!2p'=k+2)
        // !2*0 = k + 2 is already deduced
        val base = Negation(Equals(Mult(z(2), Zero()), z(k + 2)))
        println(Implication(base, Implication(All(ArithmeticVariable("p"), transition), Conjunction(base, All(ArithmeticVariable("p"), transition)))).print())
        println(Implication(All(ArithmeticVariable("p"), transition), Conjunction(base, All(ArithmeticVariable("p"), transition))).print())
        println(Conjunction(base, All(ArithmeticVariable("p"), transition)).print())
        println(Implication(Conjunction(base, All(ArithmeticVariable("p"), transition)), Negation(Equals(Mult(z(2), ArithmeticVariable("p")), z(k + 2)))).print())
        println(Negation(Equals(Mult(z(2), ArithmeticVariable("p")), z(k + 2))).print()) // !2*p = k + 2
    }

    fun gen2ZNot(k: Int) {
        assert(k % 2 == 1)
        var proof = mutableListOf<Expression>()
        genAxiom(7, mapOf("a" to z(2)), true, proof) // 2*0=0
        proof.add(Equals(Mult(z(2), Zero()), z(k))) // 2*0 = k
        transEq(Mult(z(2), Zero()), z(k), Zero(), true, proof) // k = 0
        genAxiom(4, mapOf("a" to z(k - 1)), true, proof) // ! k= 0
        val assumption = Equals(Mult(z(2), Zero()), z(k))
        deduct(proof, listOf(), assumption) //
        val strange = Equals(z(k), Zero())
        println(Implication(Implication(assumption, strange), Implication(Implication(assumption, Negation(strange)), Negation(assumption))).print())
        println(Implication(Implication(assumption, Negation(strange)), Negation(assumption)).print())
        println(Negation(assumption).print())
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ProofGenerator().start()
        }
    }
}