package lang

import cc.ekblad.konbini.ParserResult
import cc.ekblad.konbini.parseToEnd
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File

class Compiler : CliktCommand() {
    private val inFile by option("-i", "--in", help="Input File").required()
    private val outFile by option("-o", "--out", help="Output File").required()

    override fun run() {

        val content = File(inFile).readText()
        val parser = TLParser()
        val prog = when (val p = parser.programP.parseToEnd(content, true)) {
            is ParserResult.Ok -> p.result
            is ParserResult.Error -> error(p)
        }
        println(prog)

        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        cw.visit(
            Opcodes.V17,
            Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
            "Main",
            null,
            "java/lang/Object",
            emptyArray()
        )

        val emitter = TLEmitter()
        emitter.emitModule(prog, cw)

        cw.visitEnd()

        val out = cw.toByteArray()

        val f = File(outFile)
        f.writeBytes(out)
    }
}

fun main(args: Array<String>) = Compiler().main(args)