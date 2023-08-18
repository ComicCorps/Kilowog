package github.buriedincode.kilowog.models.metroninfo.enums

import github.buriedincode.kilowog.Utils.titleCase
import kotlinx.serialization.SerialName

enum class AgeRating {
    @SerialName("Unknown")
    UNKNOWN,

    @SerialName("Everyone")
    EVERYONE,

    @SerialName("Teen")
    TEEN,

    @SerialName("Teen Plus")
    TEEN_PLUS,

    @SerialName("Mature")
    MATURE,

    ;

    override fun toString(): String {
        return this.titleCase()
    }
}
