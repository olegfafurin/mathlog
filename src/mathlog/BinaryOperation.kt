package mathlog

abstract class BinaryOperation : Expression {
    abstract val lhs: Expression
    abstract val rhs: Expression

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this.javaClass.name == other.javaClass.name) return (this.lhs == (other as BinaryOperation).lhs) && (this.rhs == (other as BinaryOperation).rhs)
        return false
    }

    override fun hashCode(): Int {
        var result = lhs.hashCode()
        result = 31 * result + rhs.hashCode()
        return result
    }
}