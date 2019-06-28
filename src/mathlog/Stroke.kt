package mathlog

import kotlin.math.exp

/**
 * Created by imd on 28/06/2019
 */
class Stroke(val expr : ArithmeticExpression) : ArithmeticExpression {
    override fun print() = "${expr.print()}'"
}