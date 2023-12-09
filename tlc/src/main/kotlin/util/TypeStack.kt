package util

class TypeStack(var top: StackNode<Type>) : Cloneable {

    sealed class StackNode<T> {
        class Base<T> : StackNode<T>() {
            override fun toString(): String = ""
        }
        data class Node<T>(val data: T, val next: StackNode<T>) : StackNode<T>() {
            override fun toString(): String = "$next $data"
        }

        override fun equals(other: Any?): Boolean {
            return when (this) {
                is Base -> other is Base<*>
                is Node -> other is Node<*>
                        && this.data == other.data
                        && this.next == other.next
            }
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    fun push(v: Type) {
        top = StackNode.Node(v, top)
    }

    fun pop(): Type? {
        return when (val t = top) {
            is StackNode.Base -> return null
            is StackNode.Node -> {
                val data = t.data
                top = t.next
                data
            }
        }
    }

    fun peek(): Type? {
        return when (val t = top) {
            is StackNode.Base -> null
            is StackNode.Node -> t.data
        }
    }

    fun transform(rule: TransformRule) : Boolean {
        print("$this -> ")
        return if (rule.apply(this)) {
            println("$this")
            true
        } else {
            println("ERROR")
            false
        }
    }

    fun transformOrThrow(ruleName: String, rule: TransformRule) {
        print("$ruleName: ")
        transform(rule) || throw RuntimeException("Rule provided did not match the input stack. The rule was: $rule")
    }

    private fun resolveType(unresolved: Type, resolved: Type, binds: MutableMap<String, Type>): Boolean {
        when (unresolved) {
            is Type.Generic -> {
                val id = unresolved.ident
                val existingBind = binds[id]

                // if the existing bind doesn't match
                if (existingBind != null && existingBind != resolved) {
                    return false
                }

                if (unresolved.referenceType && resolved !is Type.Reference) {
                    return false
                }

                // Either bind matches, or bind doesn't exist
                binds[id] = resolved
                return true
            }

            else -> return unresolved == resolved
        }
    }

    // Takes input and output types, and transforms the stack based on the rules.
    // Return if the transformation was successful
    // It will also attempt to resolve generics
    fun tryTransform(input: List<Type>, output: List<Type>): Boolean {
        val other = clone()

        // bindings for generic types
        val binds = mutableMapOf<String, Type>()

        for (unresolved in input.asReversed()) {
            val resolved = other.pop() ?: return false
            if (!resolveType(unresolved, resolved, binds)) {
                return false
            }
        }

        for (t in output) {
            when (t) {
                is Type.Generic -> {
                    val resolved =
                        binds[t.ident] ?: throw RuntimeException("Output generic $t does not appear in the input list")
                    other.push(resolved)
                }

                else -> other.push(t)
            }
        }

        // if everything matches, do the transformation
        setTo(other)
        return true
    }


    public override fun clone(): TypeStack = TypeStack(top)

    private fun setTo(other: TypeStack) {
        this.top = other.top
    }

    override fun equals(other: Any?): Boolean = other is TypeStack && top == other.top

    override fun hashCode(): Int = top.hashCode()

    override fun toString(): String {
        return "[ $top ]"
    }

    companion object {
        fun empty(): TypeStack = TypeStack(StackNode.Base())
    }
}