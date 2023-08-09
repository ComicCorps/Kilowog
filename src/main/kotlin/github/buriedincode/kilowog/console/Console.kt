package github.buriedincode.kilowog.console

import org.apache.logging.log4j.kotlin.Logging

internal object Console : Logging {
    private val HEADING: Colour = Colour.BLUE
    private val HIGHLIGHT: Colour = Colour.MAGENTA
    private val PROMPT: Colour = Colour.CYAN
    private val INPUT: Colour = Colour.GREEN
    private val STANDARD: Colour = Colour.WHITE

    internal fun displayHeading(text: String) {
        colourOutput(output = "=".repeat(text.length + 4), colour = HEADING)
        displaySubHeading(text = text)
        colourOutput(output = "=".repeat(text.length + 4), colour = HEADING)
    }

    internal fun displaySubHeading(text: String) {
        colourOutput(output = "  $text  ", colour = HEADING)
    }

    internal fun displayMenu(
        title: String,
        options: List<String>,
        exit: String? = null,
        useSubheading: Boolean = false,
    ): Int {
        if (useSubheading) {
            displaySubHeading(text = title)
        } else {
            displayHeading(text = title)
        }
        if (options.isEmpty()) {
            return 0
        }
        val padCount = options.size.toString().length
        options.indices.forEach {
            displayItemValue(item = (it + 1).toString().padStart(padCount), value = options[it])
        }
        if (exit != null) {
            displayItemValue(item = "0", value = exit)
        }
        return displayPrompt(text = "Option")?.toIntOrNull() ?: 0
    }

    internal fun displayPrompt(text: String): String? {
        return Reader.readConsole(text = text, promptColour = PROMPT, inputColour = INPUT)?.trim()
    }

    internal fun displayAgreement(text: String): Boolean {
        val input = displayPrompt(text = "$text (Y/N)")
        return input.equals("y", ignoreCase = true)
    }

    internal fun displayItemValue(item: String, value: String?) {
        colourOutput(output = "$item: $STANDARD$value", colour = HIGHLIGHT)
    }

    internal fun display(text: String, colour: Colour = STANDARD) {
        colourOutput(output = text, colour = colour)
    }

    internal fun displayList(title: String, items: List<String>, isOrdered: Boolean = false) {
        colourOutput("$title:", HEADING)
        val padCount = items.size.toString().length
        items.forEachIndexed { index, item ->
            val itemIndex = if (isOrdered) (index + 1).toString().padStart(padCount) else "- "
            displayItemValue(itemIndex, item)
        }
    }

    private fun colourOutput(output: String?, colour: Colour) {
        println("$colour$output${Colour.RESET}")
    }
}
