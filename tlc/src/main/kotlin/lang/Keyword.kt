package lang

import cc.ekblad.konbini.Parser
import cc.ekblad.konbini.map
import cc.ekblad.konbini.oneOf
import cc.ekblad.konbini.string

enum class Keyword(val token: String) {

    // ARITH
    ADD("+"), SUB("-"), INC("++"), DEC("--"),
    MUL("*"), DIV("/"), MOD("%"), POW("**"),
    SHR(">>>"), SSHR(">>"), SHL("<<"),
    BAND("&"), BOR("|"), BXOR("^"), BNOT("~"),

    // BOOLEAN
    NOT("!"),
    EQ("=="), NEQ("!="),
    GT(">"), GTE(">="), LT("<"), LTE("<="),

    // BUILT-INS
    DUP("dup"), OVER("over"), SWAP("swap"), DROP("drop"),

    // BUILT-IN SYMBOLS
    ARR_START("["), ARR_END("]"),

    // DEBUG
    HOLE("??"),

    ;

    companion object {
        fun parser(): Parser<Keyword> =
            oneOf(*Keyword.values()
                .sortedBy { i -> i.token.length }
                .reversed()
                .map { i -> string(i.token).map { i } }.toTypedArray())
    }
}