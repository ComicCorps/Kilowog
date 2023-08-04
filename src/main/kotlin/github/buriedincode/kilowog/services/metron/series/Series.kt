package github.buriedincode.kilowog.services.metron.series

import com.fasterxml.jackson.annotation.JsonAlias

data class Series(
    @JsonAlias("id")
    val seriesId: Int,
    val name: String,
    @JsonAlias("sort_name")
    val sortName: String,
    val volume: Int,
    @JsonAlias("series_type")
    val type: SeriesType,
    val publisher: Publisher,
    @JsonAlias("year_began")
    val yearBegan: Int,
    @JsonAlias("year_end")
    val yearEnd: Int,
    @JsonAlias("desc")
    val description: String? = null,
    @JsonAlias("issue_count")
    val issueCount: Int,
    val genres: List<Genre> = emptyList(),
    val associated: List<Associated> = emptyList(),
    @JsonAlias("resource_url")
    val resourceUrl: String,
    @JsonAlias("modified")
    val dateModified: String,
) {
    data class SeriesType(
        @JsonAlias("id")
        val seriesTypeId: Int,
        val name: String,
    )

    data class Publisher(
        @JsonAlias("id")
        val publisherId: Int,
        val name: String,
    )

    data class Genre(
        @JsonAlias("id")
        val genreId: Int,
        val name: String,
    )

    data class Associated(
        @JsonAlias("id")
        val associatedId: Int,
        val name: String,
    )
}
