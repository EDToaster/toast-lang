package lang

import cc.ekblad.konbini.*

class TLParser {

    val uintP = oneOf(
        // HEX
        parser {
            string("0x")
            regex("[0-9a-zA-Z_]+".toRegex()).stripUnder().toLong(radix = 16)
        },
        // OCT
        parser {
            string("0o")
            regex("[0-7_]+".toRegex()).stripUnder().toLong(radix = 8)
        },
        // BIN
        parser {
            string("0b")
            regex("[01_]+".toRegex()).stripUnder().toLong(radix = 2)
        },
        // DEC
        parser {
            regex("(0)|([1-9][0-9_]*)".toRegex()).stripUnder().toLong()
        },
    )

    val intP = oneOf(
        parser {
            char('-') ;
            Literal.IntLiteral(-uintP())
        },
        uintP.map(Literal::IntLiteral)
    )

    val keywordP = Keyword.parser()

    val identP = parser {

        val ident: String = regex("[_a-zA-Z][_a-zA-Z0-9]*")
        if (listOf("while", "do", "end", "if", "else").contains(ident)) {
            fail("")
        }
        ident
    }

    val whileP: Parser<Statement.While> = parser {
        string("while")
        whitespace()
        val cond = chain(statementP, whitespace1).terms
        if (cond.isNotEmpty()) {
            whitespace1()
        }
        string("do")
        whitespace1()
        val body = chain(statementP, whitespace1).terms
        if (body.isNotEmpty()) {
            whitespace1()
        }
        string("end")
        Statement.While(cond, body)
    }

    val ifP: Parser<Statement.If> = parser {
        string("if")
        whitespace()
        val trueBody = chain(statementP, whitespace1).terms
        if (trueBody.isNotEmpty()) {
            whitespace1()
        }

        val falseBody = oneOf(
            parser {
                string("else")
                whitespace1()
                val falseBody = chain(statementP, whitespace1).terms
                if (falseBody.isNotEmpty()) {
                    whitespace1()
                }
                falseBody
            },
            parser { null }
        )

        string("end")
        Statement.If(trueBody, falseBody)
    }

    val statementP: Parser<Statement> = oneOf(
        whileP, ifP,
        parser { Statement.Keyword(keywordP()) },
        parser { Statement.Literal(intP()) },
        parser { Statement.Literal(Literal.StrLiteral(doubleQuotedString())) },
        parser { Statement.Ident(identP()) },
    )

    val funcP = parser {
        string("fn")
        whitespace1()
        val id = identP()
        whitespace1()
        string("->")
        whitespace1()
        string("do")
        whitespace1()
        val body = chain(statementP, whitespace1).terms
        if (body.isNotEmpty()) {
            whitespace1()
        }
        string("end")
        CodeUnit.Func(id, body)
    }

    val inlineP = parser {
        string("inline")
        whitespace1()
        val id = identP()
        whitespace1()
        val body = chain(statementP, whitespace1).terms
        if (body.isNotEmpty()) {
            whitespace1()
        }
        string("end")
        CodeUnit.Inline(id, body)
    }

    val moduleP = parser {
        string("module")
        whitespace1()
        val id = identP()
        val body = chain(codeUnitP, whitespace1).terms
        if (body.isNotEmpty()) {
            whitespace1()
        }
        string("end")
        CodeUnit.Module(id, body)
    }

    val codeUnitP: Parser<CodeUnit> = oneOf(
        funcP, inlineP, moduleP
    )

    val programP = parser {
        whitespace()
        val units = chain(codeUnitP, whitespace1).terms
        whitespace()
        CodeUnit.Module("default", units)
    }

}
private fun String.stripUnder(): String {
    return this.replace("_", "")
}