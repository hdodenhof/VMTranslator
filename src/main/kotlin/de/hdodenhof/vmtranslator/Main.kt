package de.hdodenhof.vmtranslator

import java.io.File
import java.io.FileWriter

fun main(args: Array<String>) {
    VMTranslator().translate(args)
}

class VMTranslator {

    fun translate(args: Array<String>) {
        val inputFile = File(args[0])
        val inputFilePath = inputFile.absolutePath.substringBeforeLast(File.separatorChar)
        val inputFileName = inputFile.name.substringBeforeLast(".")
        val outputFile = File(inputFilePath, "$inputFileName.asm")

        val commands = Parser(inputFile).parse()

        FileWriter(outputFile).buffered().use { writer ->
            commands.forEach { command ->
                command.asm().forEach { line ->
                    writer.append(line)
                    writer.newLine()
                }
            }
        }
    }
}
