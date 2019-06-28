package mathlog

/**
 * Created by imd on 28/06/2019
 */

class Predicate(val name : String, val terms : List<ArithmeticExpression>) : Expression {
    override fun print() = "$name(${terms.map { it.print() }.joinToString(separator = ", ")})"
}