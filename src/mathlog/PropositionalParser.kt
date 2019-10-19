package mathlog

class PropositionalParser(private var s: String? = null) {
    private var pos = 0
    var expr: Expression? = null

    private val LETTERS = ('A'..'Z').plus('0'..'9').plus('\'')

    fun readChar(): Char? {
        return s?.getOrNull(pos++)
    }

    fun returnChar() = pos--

    fun parseVar(): Expression? {
        var name = ""
        var buf = readChar()
        while (buf in LETTERS) {
            name += buf
            buf = readChar()
        }
        returnChar()
        return if (name != "") Variable(name) else null
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
        returnChar()
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
            '(' -> {
                val r = parseExpr()
                if (readChar() == ')') r else null
            }
            '!' -> {
                Negation(parseNeg()!!)
            }
            else -> {
                returnChar()
                parseVar()
            }
        }
    }

    fun start(): Expression? {
        if (s == null) s = readLine()!!.filter { it != '\t' && it != '\r' && it != ' ' }
        return parseExpr()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println(PropositionalParser().start()!!.print())
        }

        fun parse(s: String?): Expression? {
            if (s == null) return null
            return PropositionalParser(s).start()
        }
    }
}

