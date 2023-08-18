package github.buriedincode.kilowog.models.metadata.enums

import github.buriedincode.kilowog.Utils.titleCase
import kotlinx.serialization.SerialName

enum class Source {
    @SerialName("Comicvine")
    COMICVINE,

    @SerialName("League of Comic Geeks")
    LEAGUE_OF_COMIC_GEEKS,

    @SerialName("Marvel")
    MARVEL,

    @SerialName("Metron")
    METRON,

    ;

    override fun toString(): String {
        return this.titleCase()
    }
}
