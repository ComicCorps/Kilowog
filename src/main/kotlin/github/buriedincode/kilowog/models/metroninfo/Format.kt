package github.buriedincode.kilowog.models.metroninfo

import github.buriedincode.kilowog.Utils.titleCase
import kotlinx.serialization.SerialName

enum class Format {
    @SerialName("Annual")
    ANNUAL,

    @SerialName("Graphic Novel")
    GRAPHIC_NOVEL,

    @SerialName("Limited")
    LIMITED,

    @SerialName("One-Shot")
    ONE_SHOT,

    @SerialName("Series")
    SERIES,

    @SerialName("Trade Paperback")
    TRADE_PAPERBACK,

    ;

    override fun toString(): String {
        return this.titleCase()
    }
}
