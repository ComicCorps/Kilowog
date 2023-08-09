package github.buriedincode.kilowog.console

import java.util.Scanner

internal object Reader {
    private val READER = Scanner(System.`in`)

    fun readConsole(text: String, promptColour: Colour, inputColour: Colour): String? {
        print("${promptColour.ansiCode}$text >> ${inputColour.ansiCode}")
        val input = readlnOrNull()
        print(Colour.RESET.ansiCode)
        return input
    }
}
