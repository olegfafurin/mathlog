package mathlog

class Variable(val name : String): Expression {
    override fun print() = name

    override fun toString() = name

    override fun equals(other: Any?): Boolean {
        if (other is Variable) return name == other.name
        return false
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}