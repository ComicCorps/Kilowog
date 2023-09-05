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
    val comicvineId: Long? = null,
    @JsonNames("modified")
    @Serializable(with = OffsetDateTimeSerializer::class)
    val dateModified: LocalDateTime,
    @JsonNames("desc")
    val description: String? = null,
    val genres: List<Genre> = emptyList(),
    val id: Long,
    val issueCount: Int,
    val name: String,
    val publisher: Publisher,
    val resourceUrl: String,
    val seriesType: SeriesType,
    val sortName: String,
    val volume: Int,
    val yearBegan: Int,
    val yearEnd: Int? = null,
) {
    @Serializable
    data class SeriesType(
        val id: Long,
        val name: String,
    )

    @Serializable
    data class Publisher(
        val id: Long,
        val name: String,
    )

    @Serializable
    data class Genre(
        val id: Long,
        val name: String,
    )

    @Serializable
    data class Associated(
        val id: Long,
        @JsonNames("series")
        val name: String,
    )
}
