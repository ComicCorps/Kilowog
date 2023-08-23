package github.buriedincode.kilowog.services.metron.series

import github.buriedincode.kilowog.OffsetDateTimeSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.LocalDateTime

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Series(
    val associated: List<Associated> = emptyList(),
    @JsonNames("cv_id")
    val comicvineId: Int? = null,
    @JsonNames("modified")
    @Serializable(with = OffsetDateTimeSerializer::class)
    val dateModified: LocalDateTime,
    @JsonNames("desc")
    val description: String? = null,
    val genres: List<Genre> = emptyList(),
    val issueCount: Int,
    val name: String,
    val publisher: Publisher,
    val resourceUrl: String,
    @JsonNames("id")
    val seriesId: Int,
    val seriesType: SeriesType,
    val sortName: String,
    val volume: Int,
    val yearBegan: Int,
    val yearEnd: Int? = null,
) {
    @Serializable
    data class SeriesType(
        val name: String,
        @JsonNames("id")
        val seriesTypeId: Int,
    )

    @Serializable
    data class Publisher(
        val name: String,
        @JsonNames("id")
        val publisherId: Int,
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
        @JsonNames("series")
        val name: String,
    )
}
