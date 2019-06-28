package mathlog

/**
 * Created by imd on 28/06/2019
 */
class Mult(val lhs : ArithmeticExpression, val rhs : ArithmeticExpression): ArithmeticExpression {
    override fun print() = "${lhs.print()} * ${rhs.print()}"
}