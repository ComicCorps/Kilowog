package github.buriedincode.kilowog.models.comicinfo

import github.buriedincode.kilowog.Utils.titleCase
import kotlinx.serialization.SerialName

enum class Manga {
    @SerialName("Yes")
    YES,

    @SerialName("No")
    NO,

    @SerialName("Unknown")
    UNKNOWN,

    @SerialName("YesAndRightToLeft")
    YES_AND_RIGHT_TO_LEFT,

    ;

    override fun toString(): String {
        return this.titleCase()
    }
}
