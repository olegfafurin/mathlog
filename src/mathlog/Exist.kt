package mathlog

/**
 * Created by imd on 28/06/2019
 */
class Exist(val x : ArithmeticVariable, val expression: Expression) : Expression {
    override fun print() = "(?${x.print()}.${expression.print()})"
}