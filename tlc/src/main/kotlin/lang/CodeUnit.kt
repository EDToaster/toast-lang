package lang

sealed class CodeUnit {

    data class Inline(val name: String, val body: List<Statement>): CodeUnit()
    data class Func(val name: String, val body: List<Statement>): CodeUnit()
    data class Module(val name: String, val nested: List<CodeUnit>): CodeUnit()
}