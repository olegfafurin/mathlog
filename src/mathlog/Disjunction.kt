package mathlog

class Disjunction(override val lhs: Expression, override val rhs: Expression) : BinaryOperation() {

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Disjunction) return false
        return (lhs == other.lhs) && (rhs == other.rhs)
    }

    override fun print() = "(${lhs.print()} | ${rhs.print()})"

    override fun toString(): String {
        return "(|,$lhs,$rhs)";
    }
}