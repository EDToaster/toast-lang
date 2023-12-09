package lang

sealed class Statement {
    data class Keyword(val keyword: lang.Keyword): Statement()
    data class Literal(val value: lang.Literal): Statement()
    data class Ident(val ident: String): Statement()

    data class While(val cond: List<Statement>, val body: List<Statement>): Statement()
    data class If(val trueBody: List<Statement>, val falseBody: List<Statement>?): Statement()
}