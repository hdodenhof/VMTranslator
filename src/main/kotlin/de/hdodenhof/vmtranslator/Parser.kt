package de.hdodenhof.vmtranslator

import java.io.File
import java.io.FileReader

class Parser(private val file: File) {

    private val fileName = file.name.substringBeforeLast(".")

    fun parse(): ArrayList<Command> {
        val commands = arrayListOf<Command>()
        iterateLines(file) { line ->
            commands.add(parseLine(line))
        }

        return commands
    }

    private fun iterateLines(file: File, lineAction: (String) -> Unit) {
        FileReader(file).buffered().useLines { lines ->
            lines.forEach {
                val trimmed = it.trim()
                when {
                    trimmed.isBlank() -> return@forEach
                    trimmed.startsWith("//") -> return@forEach
                    else -> {
                        lineAction.invoke(trimmed)
                    }
                }
            }
        }
    }

    private fun parseLine(line: String): Command {
        return when {
            line.startsWith("push") -> parsePushPopCommand(line)
            line.startsWith("pop") -> parsePushPopCommand(line)
            line.startsWith("add") -> AddCommand()
            line.startsWith("sub") -> SubCommand()
            line.startsWith("neg") -> NegCommand()
            line.startsWith("eq") -> EqCommand()
            line.startsWith("gt") -> GtCommand()
            line.startsWith("lt") -> LtCommand()
            line.startsWith("and") -> AndCommand()
            line.startsWith("or") -> OrCommand()
            line.startsWith("not") -> NotCommand()
            else -> throw NotImplementedError()
        }
    }

    private fun parsePushPopCommand(line: String): Command {
        val elements = line.split(" ")
        val cmd = elements[0]
        val segment = elements[1]
        val index = elements[2].toInt()

        return when (cmd) {
            "push" -> PushCommand(segment, index, fileName)
            "pop" -> PopCommand(segment, index, fileName)
            else -> throw RuntimeException("Invalid push/pop command")
        }
    }
}
