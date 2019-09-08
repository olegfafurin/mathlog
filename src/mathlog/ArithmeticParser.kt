package mathlog

/**
 * Created by imd on 28/06/2019
 */


class ArithmeticParser(private var s: String? = null) {
    private var pos = 0
    var expr: Expression? = null

    private val BIG_LETTERS = ('A'..'Z')
    private val SMALL_LETTERS = ('a'..'z')
    private val DIGITS = ('0'..'9')

    fun readChar(): Char? = s?.getOrNull(pos++)

    fun returnChar() = pos--

    fun parseString(): String {
        var s = ""
        var buf = readChar()
        while (buf in BIG_LETTERS.union(DIGITS)) {
            s += buf
            buf = readChar()
        }
        returnChar()
        return s
    }

    fun parseVar(): ArithmeticVariable? {
        var name = ""
        var buf = readChar()
        while (buf in SMALL_LETTERS.union(DIGITS)) {
            name += buf
            buf = readChar()
        }
        returnChar()
        return if (name != "") ArithmeticVariable(name) else null
    }

    fun parseExpr(): Expression? {
        if (s == null || s == "") return null
        val left = parseDisj(null)
        val next = readChar() ?: return left
        if (next == '-') {
            if (readChar() == '>' && left != null) {
                return Implication(left, parseExpr()!!)
            }
            returnChar()
        }
        if (next != ',') returnChar()
        return left
    }

    fun parseDisj(left: Expression?): Expression? {
        val nextTerm = parseConj(null)
        val next = readChar()
        if (next == '|') return parseDisj(if (left == null) nextTerm else Disjunction(left, nextTerm!!))
        if (next != null) returnChar()
        return if (left == null) nextTerm else Disjunction(left, nextTerm!!)
    }

    fun parseConj(left: Expression?): Expression? {
        val nextTerm = parseNeg()
        val next = readChar()
        if (next == '&') return parseConj(if (left == null) nextTerm else Conjunction(left, nextTerm!!))
        if (next != null) returnChar()
        return if (left == null) nextTerm else Conjunction(left, nextTerm!!)
    }

    fun parseNeg(): Expression? {
        return when (readChar()) {
//            '(' -> {
//                val r = parseExpr()
//                if (readChar() == ')') r else null
//            }
            '!' -> {
                Negation(parseNeg()!!)
            }
            '@' -> {
                val a = parseVar()
                if (readChar() == '.') All(a!!, parseExpr()!!)
                else {
                    returnChar()
                    All(a!!, parseNeg()!!)
                }
            }
            '?' -> {
                val a = parseVar()
                if (readChar() == '.') Exist(a!!, parseExpr()!!)
                else {
                    returnChar()
                    Exist(a!!, parseNeg()!!)
                }
            }
            else -> {
                returnChar()
                parsePredicate()
            }
        }
    }

    private fun parsePredicate(): Expression? {
        val s = readChar()
        returnChar()
        when (s) {
            in BIG_LETTERS -> {
                var name = parseString()
                var terms = mutableListOf<ArithmeticExpression>()
                if (readChar() != '(') {
                    returnChar()
                    return Predicate(name, terms)
                }
                while (readChar() != ')') {
                    returnChar()
                    while (readChar() == ',') continue
                    returnChar()
                    terms.add(parseTerm(null)!!)
                }
                return Predicate(name, terms)
            }
            else -> {
                val lhs = parseTerm(null)
                if (lhs == null) {
                    val c = readChar()
                    if (c == '(') {
                        val ans = parseExpr()
                        if (readChar() == ')') return ans
                        else {
                            returnChar()
                            return null
                        }
                    }
                    else {
                        returnChar()
                        return null
                    }
                }
                else {
                    val c = readChar()
                    if (c != '=') {

                        return null
                    }
                    val rhs = parseTerm(null)
                    return Equals(lhs, rhs!!)
                }
            }
        }
    }

    private fun parseTerm(left: ArithmeticExpression?): ArithmeticExpression? {
        val nextTerm = parseAdd(null)
        val next = readChar()
        if (next == '+') return parseTerm(if (left == null) nextTerm else Add(left, nextTerm!!))
        if (next != null) returnChar()
        return if (left == null) nextTerm else Add(left, nextTerm!!)
    }

    private fun parseAdd(left: ArithmeticExpression?): ArithmeticExpression? {
        val nextTerm = parseMult(null)
        val next = readChar()
        if (next == '*') return parseAdd(if (left == null) nextTerm else Mult(left, nextTerm!!))
        if (next != null) returnChar()
        return if (left == null) nextTerm else Mult(left, nextTerm!!)
    }

    private fun parseMult(left: ArithmeticExpression?): ArithmeticExpression? {
        val c = readChar()
//        returnChar()
        if (left != null) {
            if (c == '\'') return parseMult(Stroke(left))
            else {
                returnChar()
                return left
            }
        }
        when (c) {
            '0' -> return parseMult(Zero())
            '(' -> {
                val oldPos = pos
                val t = parseTerm(null)
                return if (readChar() == ')') parseMult(t)
                else {
                    pos = oldPos
                    returnChar()
                    null
                }
            }
            in SMALL_LETTERS -> {
                var current : String = c.toString()
                var buf = readChar()
                while (buf in SMALL_LETTERS.union(DIGITS)) {
                    current += buf
                    buf = readChar()
                }
                returnChar()
                if (readChar() == '(') {
                    var terms = mutableListOf<ArithmeticExpression>()
                    while (readChar() != ')') {
                        returnChar()
                        if (readChar() != ',') returnChar()
                        terms.add(parseTerm(null)!!)
                    }
                    return parseMult(Function(current, terms))
                } else {
                    returnChar()
                    return parseMult(ArithmeticVariable(current))
                }
            }
        }
        returnChar()
        return null
    }

    fun start(): Expression? {
        if (s == null) s = readLine()!!.filter { it != '\t' && it != '\r' && it != ' ' }
        expr = parseExpr()
//        println(expr)
        return expr
//    parse()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println(ArithmeticParser().start()!!.print())
        }

        fun parse(s: String?): Expression? {
            if (s == null) return null
            return ArithmeticParser(s).start()
        }
    }
}
