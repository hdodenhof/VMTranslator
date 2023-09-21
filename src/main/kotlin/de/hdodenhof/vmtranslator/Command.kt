package de.hdodenhof.vmtranslator

import java.util.concurrent.atomic.AtomicInteger

interface Command {
    fun asm(): ArrayList<String>
}

abstract class PushPopCommand(segmentString: String, protected val index: Int, protected val filename: String) :
    Command {

    protected val segment = Segment.fromString(segmentString)

    protected fun determineAddress(): ArrayList<String> {
        val asm = arrayListOf<String>()

        when (segment) {
            Segment.LOCAL, Segment.ARGUMENT, Segment.THIS, Segment.THAT -> {
                asm.add("@${segment.startSymbol}")
                asm.add("D=M")
            }

            Segment.POINTER, Segment.TEMP -> {
                asm.add("@${segment.startSymbol}")
                asm.add("D=A")
            }

            else -> throw InvalidCommandException()
        }

        asm.add("@$index")
        asm.add("D=D+A")

        return asm
    }
}

class PushCommand(segmentString: String, index: Int, filename: String) :
    PushPopCommand(segmentString, index, filename) {

    override fun asm(): ArrayList<String> {
        val asm = arrayListOf<String>()
        asm.add("// push ${segment.name.lowercase()} $index")

        when (segment) {
            Segment.CONSTANT -> {
                asm.add("@$index")
                asm.add("D=A")
            }

            Segment.LOCAL, Segment.ARGUMENT, Segment.THIS, Segment.THAT, Segment.POINTER, Segment.TEMP -> {
                asm.addAll(determineAddress())
                asm.add("A=D")
                asm.add("D=M")
            }

            Segment.STATIC -> {
                asm.add("@$filename.$index")
                asm.add("D=M")
            }
        }

        asm.add("@SP")
        asm.add("A=M")
        asm.add("M=D")
        asm.add("@SP")
        asm.add("M=M+1")

        return asm
    }

}

class PopCommand(segmentString: String, index: Int, filename: String) :
    PushPopCommand(segmentString, index, filename) {

    override fun asm(): ArrayList<String> {
        val asm = arrayListOf<String>()
        asm.add("// pop ${segment.name.lowercase()} $index")

        when (segment) {
            Segment.CONSTANT -> throw InvalidCommandException()

            Segment.LOCAL, Segment.ARGUMENT, Segment.THIS, Segment.THAT, Segment.POINTER, Segment.TEMP -> {
                asm.addAll(determineAddress())
            }

            Segment.STATIC -> {
                asm.add("@$filename.$index")
                asm.add("D=A")
            }
        }

        asm.add("@R13")
        asm.add("M=D")
        asm.add("@SP")
        asm.add("M=M-1")
        asm.add("A=M")
        asm.add("D=M")
        asm.add("@R13")
        asm.add("A=M")
        asm.add("M=D")

        return asm
    }
}

abstract class ArithmeticCommand(private val type: Type) : Command {
    override fun asm(): ArrayList<String> {
        val asm = arrayListOf<String>()

        asm.add("// ${type.name.lowercase()}")
        asm.add("@SP")
        asm.add("M=M-1")
        asm.add("A=M")
        asm.add("D=M")
        asm.add("A=A-1")
        asm.add("M=${type.operation}")

        return asm
    }

    enum class Type(val operation: String) {
        ADD("M+D"), SUB("M-D"), AND("M&D"), OR("M|D")
    }
}

class AddCommand : ArithmeticCommand(Type.ADD)
class SubCommand : ArithmeticCommand(Type.SUB)
class AndCommand : ArithmeticCommand(Type.AND)
class OrCommand : ArithmeticCommand(Type.OR)

abstract class UnaryCommand(private val type: Type) : Command {
    override fun asm(): ArrayList<String> {
        val asm = arrayListOf<String>()

        asm.add("// ${type.name.lowercase()}")
        asm.add("@SP")
        asm.add("A=M-1")
        asm.add("M=${type.operation}")

        return asm
    }

    enum class Type(val operation: String) {
        NEG("-M"), NOT("!M")
    }
}

class NegCommand : UnaryCommand(Type.NEG)
class NotCommand : UnaryCommand(Type.NOT)

abstract class ComparisonCommand(private val type: Type) : Command {
    override fun asm(): ArrayList<String> {
        val cmpCount = CMP_COUNT.getAndIncrement()

        val asm = arrayListOf<String>()

        asm.add("// ${type.name.lowercase()}")
        asm.add("@SP")
        asm.add("M=M-1")
        asm.add("A=M")
        asm.add("D=M")
        asm.add("A=A-1")
        asm.add("D=M-D")
        asm.add("M=-1")
        asm.add("@CMP_$cmpCount")
        asm.add("D;${type.operation}")
        asm.add("@SP")
        asm.add("A=M-1")
        asm.add("M=0")
        asm.add("(CMP_$cmpCount)")

        return asm
    }

    enum class Type(val operation: String) {
        EQ("JEQ"), GT("JGT"), LT("JLT")
    }

    companion object {
        val CMP_COUNT = AtomicInteger()
    }
}

class EqCommand : ComparisonCommand(Type.EQ)
class GtCommand : ComparisonCommand(Type.GT)
class LtCommand : ComparisonCommand(Type.LT)
