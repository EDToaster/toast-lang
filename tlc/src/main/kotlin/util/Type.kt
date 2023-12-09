package util

sealed class Type {
    // Int Primitives
    object Int: Type() {
        override fun toString(): String = "I"
    }

    object Bool: Type() {
        override fun toString(): String = "B"
    }

    // Generics
    data class Generic(val ident: String): Type() {
        var referenceType = false

        fun reference(): Generic {
            referenceType = true
            return this
        }

        override fun toString(): String = "\$${if (referenceType) { "*" } else { "" }}$ident"
    }

    // Can be a generic reference
    data class Reference(val type: String): Type() {
        override fun toString(): String = "*$type"
    }

    data class Array(val type: Type): Type() {
        override fun toString(): String = "[$type"
    }

    // Can be a generic symbol
    data class Symbol(val ident: String): Type() {
        override fun toString(): String = ":$ident"
    }

    // Function Type
    data class Func(val inTypes: List<Type>, val outTypes: List<Type>): Type()
}