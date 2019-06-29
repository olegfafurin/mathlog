package mathlog

abstract class BinaryOperation : Expression {
    abstract val lhs: Expression
    abstract val rhs: Expression

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (javaClass.name == other.javaClass.name) return (this.lhs == (other as BinaryOperation).lhs) && (this.rhs == other.rhs)
        return false
    }

    override fun hashCode(): Int {
        var result = javaClass.hashCode()
        result = 31 * result + lhs.hashCode()
        result = 31 * result + rhs.hashCode()
        return result
    }
}

class Conjunction(override val lhs: Expression, override val rhs: Expression) : BinaryOperation() {

    override fun print() = "(${lhs.print()} & ${rhs.print()})"

    override fun toString(): String {
        return "(&,$lhs,$rhs)";
    }

}

class Implication(override val lhs: Expression, override val rhs: Expression) : BinaryOperation() {

    override fun print() = "(${lhs.print()} -> ${rhs.print()})"

    override fun toString(): String {
        return "(->,$lhs,$rhs)";
    }
}

class Disjunction(override val lhs: Expression, override val rhs: Expression) : BinaryOperation() {

    override fun print() = "(${lhs.print()} | ${rhs.print()})"

    override fun toString(): String {
        return "(|,$lhs,$rhs)";
    }
}

class Equals(override val lhs : ArithmeticExpression, override val rhs : ArithmeticExpression) : Expression, BinaryOperation() {
    override fun print() = "${lhs.print()} = ${rhs.print()}"
}