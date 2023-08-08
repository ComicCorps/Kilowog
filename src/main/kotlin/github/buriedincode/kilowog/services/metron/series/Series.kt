package github.buriedincode.kilowog.services.metron.series

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Series(
    @JsonNames("id")
    val seriesId: Int,
    val name: String,
    val sortName: String,
    val volume: Int,
    val seriesType: SeriesType,
    val publisher: Publisher,
    val yearBegan: Int,
    val yearEnd: Int,
    @JsonNames("desc")
    val description: String? = null,
    val issueCount: Int,
    val genres: List<Genre> = emptyList(),
    val associated: List<Associated> = emptyList(),
    val resourceUrl: String,
    @JsonNames("modified")
    val dateModified: String,
) {
    @Serializable
    data class SeriesType(
        @JsonNames("id")
        val seriesTypeId: Int,
        val name: String,
    )

    @Serializable
    data class Publisher(
        @JsonNames("id")
        val publisherId: Int,
        val name: String,
    )

    @Serializable
    data class Genre(
        @JsonNames("id")
        val genreId: Int,
        val name: String,
    )

    @Serializable
    data class Associated(
        @JsonNames("id")
        val associatedId: Int,
        val name: String,
    )
}
