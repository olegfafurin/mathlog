package mathlog

/**
 * Created by imd on 28/06/2019
 */
class ArithmeticVariable(val name : String) : ArithmeticExpression {
    override fun print() = name
}