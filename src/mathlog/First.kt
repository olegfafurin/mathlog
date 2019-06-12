package mathlog

class First(private var s: String? = null) {
    private var pos = 0
    var expr : Expression? = null

    private val LETTERS = listOf(
        'a',
        'b',
        'c',
        'd',
        'e',
        'f',
        'g',
        'h',
        'i',
        'j',
        'k',
        'l',
        'm',
        'n',
        'o',
        'p',
        'q',
        'r',
        's',
        't',
        'u',
        'v',
        'w',
        'x',
        'y',
        'z',
        'A',
        'B',
        'C',
        'D',
        'E',
        'F',
        'G',
        'H',
        'I',
        'J',
        'K',
        'L',
        'M',
        'N',
        'O',
        'P',
        'Q',
        'R',
        'S',
        'T',
        'U',
        'V',
        'W',
        'X',
        'Y',
        'Z',
        '\'',
        '0',
        '1',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
        '9'
    )

    fun readChar(): Char? = s?.getOrNull(pos++)

    fun returnChar() = pos--

    fun parseVar(): Expression? {
        var name = ""
        var buf = readChar()
        while (buf in LETTERS) {
            name += buf
            buf = readChar()
        }
        returnChar()
        return Variable(name)
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
    fun start() : Expression? {
        if (s == null) s = readLine()!!.filter { it != '\t' && it != '\r' && it != ' ' }
        expr = parseExpr()
//        println(expr)
        return expr
//    parse()
    }
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            First().start()
        }

        fun parse(s : String?) : Expression? {
            if (s == null) return null
            return First(s).start()
        }
    }
}
