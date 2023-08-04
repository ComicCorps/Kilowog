package github.buriedincode.kilowog.services.metron.series

import com.fasterxml.jackson.annotation.JsonAlias

data class SeriesEntry(
    @JsonAlias("id")
    val seriesId: Int,
    @JsonAlias("series")
    var name: String,
    @JsonAlias("modified")
    var dateModified: String,
)
