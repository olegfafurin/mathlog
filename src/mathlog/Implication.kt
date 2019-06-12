package mathlog

class Implication(override val lhs: Expression, override val rhs: Expression) : BinaryOperation(){
    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun print() = "(${lhs.print()} -> ${rhs.print()})"

    override fun toString(): String {
        return "(->,$lhs,$rhs)";
    }
}