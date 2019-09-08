package mathlog

interface Expression {
    fun print() : String
}

class Variable(val name : String, private val instantHash: Int = name.hashCode()): Expression {
    override fun print() = name

    override fun toString() = name

    override fun equals(other: Any?): Boolean {
        if (other is Variable) return name == other.name
        return false
    }

    override fun hashCode(): Int {
        return instantHash
    }
}

class Negation(val expression: Expression, private val instantHash: Int = "Neg".hashCode() xor expression.hashCode()) : Expression {
    override fun print() = "!(${expression.print()})"

    override fun toString() = "!($expression)"

    override fun equals(other: Any?): Boolean {
        if (other is Negation) return expression == other.expression
        return false
    }

    override fun hashCode(): Int {
        return instantHash
    }
}

class Exist(val variable : ArithmeticVariable, val expression: Expression, private val instantHash: Int = variable.hashCode() xor expression.hashCode().inv()) : Expression {
    override fun print() = "(?${variable.print()}.${expression.print()})"

    override fun equals(other: Any?): Boolean {
        return other is Exist && other.variable == variable && other.expression == expression
    }

    override fun hashCode() = instantHash
}

class All(val variable : ArithmeticVariable, val expression: Expression, private val instantHash: Int = variable.hashCode() xor expression.hashCode()) : Expression {
    override fun print() = "(@${variable.print()}.${expression.print()})"

    override fun equals(other: Any?): Boolean {
        return other is All && other.variable == variable && other.expression == expression
    }

    override fun hashCode() = instantHash
}

class Predicate(val name : String, val terms : List<ArithmeticExpression>, private val instantHash: Int = name.hashCode() xor terms.hashCode()) : Expression {
    override fun print(): String {
        return if (terms.isNotEmpty()) "$name${terms.joinToString(separator = ", ",prefix = "(", postfix = ")") { it.print() }}"
        else name
    }

    override fun equals(other: Any?): Boolean {
        return other is Predicate && this.name == other.name && this.terms == other.terms
    }

    override fun hashCode() = instantHash
}