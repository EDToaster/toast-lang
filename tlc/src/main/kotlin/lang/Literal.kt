package lang

sealed class Literal {
    data class IntLiteral(val v: Long) : Literal()
    data class BoolLiteral(val v: Boolean) : Literal()
    data class StrLiteral(val v: String) : Literal()

    fun emitValue(): Any = when (this) {
        is IntLiteral -> v.toInt()
        is StrLiteral -> v
        is BoolLiteral -> v
    }
}