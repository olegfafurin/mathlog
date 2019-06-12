package mathlog

class Conjunction(override val lhs: Expression, override val rhs: Expression) : BinaryOperation() {

    override fun print() = "(${lhs.print()} & ${rhs.print()})"

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Conjunction) return false
        return (lhs == other.lhs) && (rhs == other.rhs)
    }

    override fun toString(): String {
        return "(&,$lhs,$rhs)";
    }
}