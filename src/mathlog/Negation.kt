package mathlog

class Negation(val expression: Expression) : Expression {
    override fun print() = "!${expression.print()}"

    override fun toString() = "(!$expression)"

    override fun equals(other: Any?): Boolean {
        if (other is Negation) return expression == other.expression
        return false
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}