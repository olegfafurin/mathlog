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
        return javaClass.hashCode()
    }
}

class ArithmeticVariable(val name: String) : ArithmeticExpression {
    override fun print() = name
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        return (other is ArithmeticVariable && name == other.name)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

class Stroke(val expr : ArithmeticExpression) : ArithmeticExpression {
    override fun print() = "${expr.print()}'"

    override fun equals(other: Any?): Boolean {
        return (other is Stroke && expr == other.expr)
    }
    override fun hashCode(): Int {
        return expr.hashCode()
    }

}
class Function(val name : String, val terms : List<ArithmeticExpression>) : ArithmeticExpression {
    override fun print() = "$name(${terms.map { it.print() }.joinToString(separator = ", ")})"
}

class Add(override val lhs : ArithmeticExpression, override val rhs : ArithmeticExpression): ArithmeticExpression, BinaryOperation() {
    override fun print() = "(${lhs.print()} + ${rhs.print()})"
}

class Mult(override val lhs : ArithmeticExpression, override val rhs : ArithmeticExpression): ArithmeticExpression, BinaryOperation() {
    override fun print() = "(${lhs.print()} * ${rhs.print()})"
}