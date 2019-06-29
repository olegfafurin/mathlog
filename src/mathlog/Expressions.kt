package mathlog

interface Expression {
    fun print() : String
}

class Variable(val name : String): Expression {
    override fun print() = name

    override fun toString() = name

    override fun equals(other: Any?): Boolean {
        if (other is Variable) return name == other.name
        return false
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

class Negation(val expression: Expression) : Expression {
    override fun print() = "!${expression.print()}"

    override fun toString() = "(!$expression)"

    override fun equals(other: Any?): Boolean {
        if (other is Negation) return expression == other.expression
        return false
    }

    override fun hashCode(): Int {
        return javaClass.hashCode() * 31 + expression.hashCode()
    }
}

class Exist(val variable : ArithmeticVariable, val expression: Expression) : Expression {
    override fun print() = "(?${variable.print()}.${expression.print()})"
}

class All(val variable : ArithmeticVariable, val expression: Expression) : Expression {
    override fun print() = "(@${variable.print()}.${expression.print()})"
}

class Predicate(val name : String, val terms : List<ArithmeticExpression>) : Expression {
    override fun print() = "$name(${terms.joinToString(separator = ", ") { it.print() }})"
}