package util

class Rule(private val input: List<Type>, private val output: List<Type>, val f: () -> Unit): TransformRule {
    override fun apply(s: TypeStack): Boolean {
        if (s.tryTransform(input, output)) {
            f()
            return true
        }
        return false
    }

    override fun toString(): String = "{ $input -> $output }"
}