package github.buriedincode.kilowog.console

import org.apache.logging.log4j.kotlin.Logging

internal object Console : Logging {
    private val HEADING: Colour = Colour.BLUE
    private val HIGHLIGHT: Colour = Colour.MAGENTA
    private val PROMPT: Colour = Colour.CYAN
    private val STANDARD: Colour = Colour.WHITE

    internal fun title(value: String, colour: Colour = HEADING) {
        colourOutput(value = "=".repeat(value.length + 4), colour = colour)
        heading(value = value, colour = colour)
        colourOutput(value = "=".repeat(value.length + 4), colour = colour)
    }

    internal fun heading(value: String, colour: Colour = HEADING) {
        colourOutput(value = "  $value  ", colour = colour)
    }

    internal fun menu(
        title: String? = null,
        titleColour: Colour = HEADING,
        choices: List<String>,
        choiceColours: Pair<Colour, Colour> = HIGHLIGHT to STANDARD,
        default: String? = null,
        prompt: String = "Select",
        promptColour: Colour = PROMPT,
    ): Int {
        if (!title.isNullOrBlank()) heading(value = title, colour = titleColour)
        if (choices.isEmpty()) return 0
        val padCount = choices.size.toString().length
        choices.indices.forEach {
            colourPair((it + 1).toString().padStart(padCount) to choices[it], colours = choiceColours)
        }
        if (default != null) colourPair("0" to default, colours = choiceColours)
        return prompt(prompt = prompt, promptColour = promptColour)?.toIntOrNull() ?: 0
    }

    internal fun confirm(
        prompt: String,
        promptColour: Colour = PROMPT,
    ): Boolean = prompt(prompt = "$prompt (Y/N)", promptColour = promptColour)?.equals("y", ignoreCase = true) ?: false

    internal fun prompt(prompt: String, promptColour: Colour = PROMPT): String? {
        print("${promptColour}$prompt >> ${Colour.RESET}")
        return readlnOrNull()?.trim()
    }

    internal fun error(value: Any?) = colourOutput(value = value?.toString(), colour = Colour.RED)
    internal fun warn(value: Any?) = colourOutput(value = value?.toString(), colour = Colour.YELLOW)
    internal fun info(value: Any?) = colourOutput(value = value?.toString(), colour = Colour.WHITE)
    internal fun debug(value: Any?) = colourOutput(value = value?.toString(), colour = Colour.BLUE)

    private fun colourPair(
        value: Pair<String, String?>,
        colours: Pair<Colour, Colour> = HIGHLIGHT to STANDARD,
    ) = colourOutput(value = "${value.first}: ${colours.second}${value.second}", colour = colours.first)

    private fun colourOutput(value: String?, colour: Colour) = println("$colour$value${Colour.RESET}")
}
