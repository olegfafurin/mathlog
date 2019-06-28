package mathlog

/**
 * Created by imd on 28/06/2019
 */
class Equals(val lhs : ArithmeticExpression, val rhs : ArithmeticExpression) : Expression {
    override fun print() = "${lhs.print()} = ${rhs.print()}"
}