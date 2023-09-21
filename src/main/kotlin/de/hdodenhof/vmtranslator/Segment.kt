package de.hdodenhof.vmtranslator

enum class Segment(val startSymbol: String? = null) {
    LOCAL("LCL"),
    ARGUMENT("ARG"),
    THIS("THIS"),
    THAT("THAT"),
    CONSTANT,
    STATIC,
    POINTER("R3"),
    TEMP("R5");

    companion object {
        fun fromString(segmentString: String) = when (segmentString) {
            "local" -> LOCAL
            "argument" -> ARGUMENT
            "this" -> THIS
            "that" -> THAT
            "constant" -> CONSTANT
            "static" -> STATIC
            "pointer" -> POINTER
            "temp" -> TEMP
            else -> throw RuntimeException("Invalid segment")
        }
    }
}