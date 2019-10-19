package mathlog

/**
 * Created by imd on 28/06/2019
 */
interface ArithmeticExpression : Expression

class Zero: ArithmeticExpression {

    override fun print() = "0"

    override fun equals(other: Any?): Boolean {
        return other is Zero
    }

    override fun hashCode(): Int {
        return "0".hashCode()
    }
}

class ArithmeticVariable(val name: String,private val instantHash: Int = name.hashCode()) : ArithmeticExpression {
    override fun print() = name

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        return (other is ArithmeticVariable && name == other.name)
    }

    override fun hashCode() = instantHash
}

class Stroke(val expr : ArithmeticExpression, private val instantHash: Int = "\'".hashCode() xor expr.hashCode()) : ArithmeticExpression {
    override fun print() = "${expr.print()}'"

    override fun equals(other: Any?): Boolean {
        return (other is Stroke && expr == other.expr)
    }
    override fun hashCode() = instantHash

}
class Function(val name : String, val terms : List<ArithmeticExpression>, private val instantHash: Int = name.hashCode() xor terms.hashCode()) : ArithmeticExpression {
    override fun print() = "$name(${terms.joinToString(separator = ", ") { it.print() }})"

    override fun equals(other: Any?): Boolean {
        return other is Function && other.name == name && other.terms == terms
    }

    override fun hashCode() = instantHash
}

class Add(override val lhs : ArithmeticExpression, override val rhs : ArithmeticExpression, private val instantHash: Int = "A".hashCode() xor lhs.hashCode() xor rhs.hashCode()): ArithmeticExpression, BinaryOperation() {
    override fun print() = "(${lhs.print()} + ${rhs.print()})"

    override fun hashCode() = instantHash

    override fun equals(other: Any?): Boolean {
        return other is Add && other.lhs == lhs && other.rhs == rhs
    }
}

class Mult(override val lhs : ArithmeticExpression, override val rhs : ArithmeticExpression, private val instantHash: Int = "M".hashCode() xor lhs.hashCode() xor rhs.hashCode()): ArithmeticExpression, BinaryOperation() {
    override fun print() = "(${lhs.print()} * ${rhs.print()})"

    override fun hashCode() = instantHash

    override fun equals(other: Any?): Boolean {
        return other is Mult && other.lhs == lhs && other.rhs == rhs
    }
}