package mathlog

/**
 * Created by imd on 28/06/2019
 */
class Function(val name : String, val terms : List<ArithmeticExpression>) : ArithmeticExpression {
    override fun print() = "$name(${terms.map { it.print() }.joinToString(separator = ", ")})"
}