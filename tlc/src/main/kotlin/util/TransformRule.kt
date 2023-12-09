package util

interface TransformRule {
    fun apply(s: TypeStack): Boolean

    class ChainedRule(private val a: TransformRule, private val b: TransformRule): TransformRule {
        override fun apply(s: TypeStack): Boolean {
            return a.apply(s) || b.apply(s)
        }

        override fun toString(): String = "{ $a OR $b }"
    }

    fun or(other: TransformRule): TransformRule {
        return ChainedRule(this, other)
    }
}