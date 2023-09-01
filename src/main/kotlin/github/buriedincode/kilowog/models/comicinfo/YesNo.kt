package github.buriedincode.kilowog.models.comicinfo

import github.buriedincode.kilowog.Utils.titleCase
import kotlinx.serialization.SerialName

enum class YesNo {
    @SerialName("Yes")
    YES,

    @SerialName("No")
    NO,

    @SerialName("Unknown")
    UNKNOWN,

    ;

    override fun toString(): String {
        return this.titleCase()
    }
}
