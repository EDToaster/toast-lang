package lang

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import util.Rule
import util.Type
import util.TypeStack

class TLEmitter {

    private fun emitJumpIfFalse(mv: MethodVisitor, typeStack: TypeStack, label: Label) {
        typeStack.transformOrThrow(
            "jump-if-false", Rule(listOf(Type.Int), listOf()) {
                mv.visitJumpInsn(Opcodes.IFEQ, label)
            }.or(Rule(listOf(Type.Generic("a").reference()), listOf()) {
                mv.visitJumpInsn(Opcodes.IFNULL, label)
            }.or(Rule(listOf(Type.Bool), listOf()) {
                mv.visitJumpInsn(Opcodes.IFEQ, label)
            }))
        )
    }

    val inlines = mutableMapOf<String, List<Statement>>()

    fun emitStatement(mv: MethodVisitor, typeStack: TypeStack, token: Statement) {
        when (token) {
            is Statement.Ident -> {
                when (val id = token.ident) {
                    "p" -> {
                        mv.visitFieldInsn(
                            Opcodes.GETSTATIC,
                            "java/lang/System",
                            "out",
                            "Ljava/io/PrintStream;"
                        )
                        mv.visitInsn(Opcodes.SWAP)
                        typeStack.transformOrThrow(
                            "p", Rule(listOf(Type.Int), listOf()) {
                                mv.visitMethodInsn(
                                    Opcodes.INVOKEVIRTUAL,
                                    "java/io/PrintStream",
                                    "println",
                                    "(I)V",
                                    false
                                )
                            }.or(Rule(listOf(Type.Bool), listOf()) {
                                mv.visitMethodInsn(
                                    Opcodes.INVOKEVIRTUAL,
                                    "java/io/PrintStream",
                                    "println",
                                    "(Z)V",
                                    false
                                )
                            }).or(Rule(listOf(Type.Generic("a")), listOf()) {
                                mv.visitMethodInsn(
                                    Opcodes.INVOKEVIRTUAL,
                                    "java/io/PrintStream",
                                    "println",
                                    "(Ljava/lang/Object;)V",
                                    false
                                )
                            })
                        )
                    }

                    else -> {
                        if (inlines[id] != null) {
                            for (s in inlines[id]!!) {
                                emitStatement(mv, typeStack, s)
                            }
                        } else {
                            TODO()
                        }
                    }
                }
            }

            is Statement.Keyword -> when (token.keyword) {

                Keyword.ADD -> typeStack.transformOrThrow("+", Rule(
                    listOf(Type.Int, Type.Int),
                    listOf(Type.Int)
                ) {
                    mv.visitInsn(Opcodes.IADD)
                })

                Keyword.SUB -> typeStack.transformOrThrow("-", Rule(
                    listOf(Type.Int, Type.Int),
                    listOf(Type.Int)
                ) {
                    mv.visitInsn(Opcodes.ISUB)
                })

                Keyword.INC -> typeStack.transformOrThrow("++", Rule(
                    listOf(Type.Int),
                    listOf(Type.Int)
                ) {
                    mv.visitInsn(Opcodes.ICONST_1)
                    mv.visitInsn(Opcodes.IADD)
                })

                Keyword.DEC -> typeStack.transformOrThrow("--", Rule(
                    listOf(Type.Int),
                    listOf(Type.Int)
                ) {
                    mv.visitInsn(Opcodes.ICONST_1)
                    mv.visitInsn(Opcodes.ISUB)
                })

                Keyword.MUL -> typeStack.transformOrThrow("*", Rule(
                    listOf(Type.Int, Type.Int),
                    listOf(Type.Int)
                ) {
                    mv.visitInsn(Opcodes.IMUL)
                })

                Keyword.DIV -> typeStack.transformOrThrow("/", Rule(
                    listOf(Type.Int, Type.Int),
                    listOf(Type.Int)
                ) {
                    mv.visitInsn(Opcodes.IDIV)
                })

                Keyword.MOD -> typeStack.transformOrThrow("%", Rule(
                    listOf(Type.Int, Type.Int),
                    listOf(Type.Int)
                ) {
                    mv.visitInsn(Opcodes.IREM)
                })

                Keyword.POW -> TODO()
                Keyword.SHR -> typeStack.transformOrThrow(">>>", Rule(
                    listOf(Type.Int, Type.Int),
                    listOf(Type.Int)
                ) {
                    mv.visitInsn(Opcodes.IUSHR)
                })

                Keyword.SSHR -> typeStack.transformOrThrow(">>", Rule(
                    listOf(Type.Int, Type.Int),
                    listOf(Type.Int)
                ) {
                    mv.visitInsn(Opcodes.ISHR)
                })

                Keyword.SHL -> typeStack.transformOrThrow("<<", Rule(
                    listOf(Type.Int, Type.Int),
                    listOf(Type.Int)
                ) {
                    mv.visitInsn(Opcodes.ISHL)
                })

                Keyword.BAND -> typeStack.transformOrThrow("&", Rule(
                    listOf(Type.Int, Type.Int),
                    listOf(Type.Int)
                ) {
                    mv.visitInsn(Opcodes.IAND)
                })

                Keyword.BOR -> typeStack.transformOrThrow("|", Rule(
                    listOf(Type.Int, Type.Int),
                    listOf(Type.Int)
                ) {
                    mv.visitInsn(Opcodes.IOR)
                })

                Keyword.BXOR -> typeStack.transformOrThrow("^", Rule(
                    listOf(Type.Int, Type.Int),
                    listOf(Type.Int)
                ) {
                    mv.visitInsn(Opcodes.IXOR)
                })

                Keyword.BNOT -> typeStack.transformOrThrow("~", Rule(
                    listOf(Type.Int),
                    listOf(Type.Int)
                ) {
                    mv.visitInsn(Opcodes.ICONST_M1)
                    mv.visitInsn(Opcodes.IXOR)
                })

                Keyword.NOT -> TODO()
                Keyword.EQ -> TODO()
                Keyword.NEQ -> TODO()
                Keyword.GT -> TODO()
                Keyword.GTE -> TODO()
                Keyword.LT -> typeStack.transformOrThrow(
                    "<", Rule(
                        listOf(Type.Int, Type.Int),
                        listOf(Type.Bool)
                    ) {
                        val emitFalse = Label()
                        val next = Label()

                        mv.visitJumpInsn(Opcodes.IF_ICMPGE, emitFalse)
                        mv.visitInsn(Opcodes.ICONST_1)
                        mv.visitJumpInsn(Opcodes.GOTO, next)
                        mv.visitLabel(emitFalse)
                        mv.visitInsn(Opcodes.ICONST_0)
                        mv.visitLabel(next)
                    })

                Keyword.LTE -> TODO()


                Keyword.DUP -> typeStack.transformOrThrow(
                    "dup", Rule(
                        listOf(Type.Generic("a")),
                        listOf(Type.Generic("a"), Type.Generic("a"))
                    ) {
                        mv.visitInsn(Opcodes.DUP)
                    }
                )

                Keyword.SWAP -> typeStack.transformOrThrow(
                    "swap", Rule(
                        listOf(Type.Generic("a"), Type.Generic("b")),
                        listOf(Type.Generic("b"), Type.Generic("a"))
                    ) {
                        mv.visitInsn(Opcodes.SWAP)
                    }
                )

                Keyword.OVER -> typeStack.transformOrThrow("over", Rule(
                    listOf(Type.Generic("a"), Type.Generic("b")),
                    listOf(Type.Generic("a"), Type.Generic("b"), Type.Generic("a")),
                ) {
                    mv.visitInsn(Opcodes.SWAP)
                    mv.visitInsn(Opcodes.DUP_X1)
                })

                Keyword.DROP -> typeStack.transformOrThrow(
                    "swap", Rule(
                        listOf(Type.Generic("a")),
                        listOf()
                    ) {
                        mv.visitInsn(Opcodes.POP)
                    }
                )

                Keyword.ARR_START -> typeStack.transformOrThrow(
                    "[", Rule(
                        listOf(),
                        listOf(Type.Symbol("["))
                    ) { }
                )

                Keyword.ARR_END -> {
                    var num = 0
                    val type = typeStack.peek() ?: error("Array construction met empty type stack")

                    while (true) {
                        val t = typeStack.pop() ?: error("Array construction met empty type stack")
                        if (t == Type.Symbol("[")) break
                        if (t != type) error("Array construction must be homogeneous")

                        num++
                    }

                    typeStack.push(Type.Array(type))

                    mv.visitLdcInsn(num)

                    // Create new array
                    when (type) {
                        is Type.Reference -> mv.visitTypeInsn(Opcodes.ANEWARRAY, "L${type.type};")
                        Type.Bool -> mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN)
                        Type.Int -> mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT)

                        is Type.Generic -> TODO()
                        is Type.Func -> TODO()
                        is Type.Symbol -> TODO()
                        is Type.Array -> TODO()
                    }

                    for (i in num - 1 downTo 0) {
                        // val arr -> arr arr index val = dup_x1 swap index swap

                        mv.visitInsn(Opcodes.DUP_X1)
                        mv.visitInsn(Opcodes.SWAP)
                        mv.visitLdcInsn(i)
                        mv.visitInsn(Opcodes.SWAP)

                        when (type) {
                            is Type.Reference -> mv.visitInsn(Opcodes.AASTORE)
                            Type.Bool -> mv.visitInsn(Opcodes.BASTORE)
                            Type.Int -> mv.visitInsn(Opcodes.IASTORE)

                            is Type.Generic -> TODO()
                            is Type.Func -> TODO()
                            is Type.Symbol -> TODO()
                            is Type.Array -> TODO()
                        }
                    }
                }

                Keyword.HOLE -> {
                    println("Current type stack at hole: $typeStack")
                }
            }

            is Statement.Literal -> {
                when (val v = token.value) {
                    is Literal.IntLiteral -> typeStack.transformOrThrow(
                        "int", Rule(
                            listOf(),
                            listOf(Type.Int)
                        ) {
                            mv.visitLdcInsn(v.emitValue())
                        }
                    )

                    is Literal.StrLiteral -> typeStack.transformOrThrow(
                        "str", Rule(
                            listOf(),
                            listOf(Type.Reference("java/lang/String"))
                        ) {
                            mv.visitLdcInsn(v.emitValue())
                        }
                    )

                    is Literal.BoolLiteral -> typeStack.transformOrThrow(
                        "bool", Rule(
                            listOf(),
                            listOf(Type.Bool)
                        ) {
                            mv.visitLdcInsn(v.emitValue())
                        }
                    )
                }
            }

            is Statement.While -> {
                val cond = Label()
                val exit = Label()

                val afterBodyStack = typeStack.clone()

                // emit cond
                mv.visitLabel(cond)
                for (t in token.cond) {
                    emitStatement(mv, afterBodyStack, t)
                }
                emitJumpIfFalse(mv, afterBodyStack, exit)

                for (t in token.body) {
                    emitStatement(mv, afterBodyStack, t)
                }
                mv.visitJumpInsn(Opcodes.GOTO, cond)
                mv.visitLabel(exit)

                if (typeStack != afterBodyStack) {
                    error("Type stacks before while condition must be the same as after the body")
                }
            }

            is Statement.If -> {
                val elseLabel = Label()
                val endLabel = Label()

                emitJumpIfFalse(mv, typeStack, elseLabel)

                val ifStack = typeStack.clone()
                for (s in token.trueBody) {
                    emitStatement(mv, ifStack, s)
                }

                mv.visitJumpInsn(Opcodes.GOTO, endLabel)
                mv.visitLabel(elseLabel)

                if (token.falseBody != null) {
                    for (s in token.falseBody) {
                        emitStatement(mv, typeStack, s)
                    }
                }
                mv.visitLabel(endLabel)

                if (ifStack != typeStack) {
                    error("Type stacks in true branch and false branch must by equal")
                }
            }
        }
    }

    fun emitFunc(func: CodeUnit.Func, cv: ClassVisitor) {
        if (func.name != "main") {
            return
        }

        val typeStack = TypeStack.empty()

        cv.visitMethod(
            Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main",
            "([Ljava/lang/String;)V", null, null
        )
            .run {
                visitCode()

                for (s in func.body) {
                    emitStatement(this, typeStack, s)
                }

                visitInsn(Opcodes.RETURN)
                visitMaxs(-1, -1);
                visitEnd()
            }
    }

    fun emitModule(mod: CodeUnit.Module, cv: ClassVisitor) {
        for (u in mod.nested) {
            when (u) {
                is CodeUnit.Func -> {
                    emitFunc(u, cv)
                }

                is CodeUnit.Inline -> {
                    inlines[u.name] = u.body
                }

                is CodeUnit.Module -> TODO()
            }
        }
    }
}
