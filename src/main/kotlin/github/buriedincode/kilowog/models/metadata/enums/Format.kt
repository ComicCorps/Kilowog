package github.buriedincode.kilowog.models.metadata.enums

import github.buriedincode.kilowog.Utils.titleCase
import kotlinx.serialization.SerialName

enum class Format {
    @SerialName("Comic")
    COMIC,

    @SerialName("Digital Chapter")
    DIGITAL_CHAPTER,

    @SerialName("Annual")
    ANNUAL,

    @SerialName("Trade Paperback")
    TRADE_PAPERBACK,

    @SerialName("Hardcover")
    HARDCOVER,

    @SerialName("Graphic Novel")
    GRAPHIC_NOVEL,

    ;

    override fun toString(): String {
        return this.titleCase()
    }
}
