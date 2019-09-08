package mathlog

abstract class BinaryOperation() : Expression {
    abstract val lhs: Expression
    abstract val rhs: Expression

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        return ((javaClass.name == other.javaClass.name) && other.hashCode() == this.hashCode())// && this.lhs == (other as BinaryOperation).lhs) && (this.rhs == other.rhs)
    }

    override fun hashCode() = javaClass.hashCode() xor lhs.hashCode() xor rhs.hashCode()
}

class Conjunction(override val lhs: Expression, override val rhs: Expression, private val instantHash: Int = "Conj".hashCode() xor lhs.hashCode() xor rhs.hashCode()) : BinaryOperation() {

    override fun print() = "((${lhs.print()}) & (${rhs.print()}))"

    override fun toString(): String {
        return "(&,$lhs,$rhs)";
    }

    override fun equals(other: Any?): Boolean {
        return other is Conjunction && lhs == other.lhs && rhs == other.rhs
    }

    override fun hashCode() = instantHash

}

class Implication(override val lhs: Expression, override val rhs: Expression, private val instantHash: Int = "Impl".hashCode() xor lhs.hashCode() xor rhs.hashCode()) : BinaryOperation() {

    override fun print() = "((${lhs.print()}) -> (${rhs.print()}))"

    override fun toString(): String {
        return "(->,$lhs,$rhs)";
    }

    override fun equals(other: Any?): Boolean {
        return other is Implication && lhs == other.lhs && rhs == other.rhs
    }

    override fun hashCode() = instantHash
}

class Disjunction(override val lhs: Expression, override val rhs: Expression, private val instantHash: Int = "Disj".hashCode() xor lhs.hashCode() xor rhs.hashCode()) : BinaryOperation() {

    override fun print() = "((${lhs.print()}) | (${rhs.print()}))"

    override fun toString(): String {
        return "(|,$lhs,$rhs)";
    }

    override fun equals(other: Any?): Boolean {
        return other is Disjunction && lhs == other.lhs && rhs == other.rhs
    }

    override fun hashCode() = instantHash
}

class Equals(override val lhs : ArithmeticExpression, override val rhs : ArithmeticExpression) : Expression, BinaryOperation() {
    override fun print() = "${lhs.print()} = ${rhs.print()}"

    override fun equals(other: Any?): Boolean {
        return other is Equals && lhs == other.lhs && rhs == other.rhs
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + lhs.hashCode()
        result = 31 * result + rhs.hashCode()
        return result
    }
}