package github.buriedincode.kilowog.services.metron.issue

import github.buriedincode.kilowog.OffsetDateTimeSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.LocalDateTime

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class IssueEntry(
    val coverDate: LocalDate,
    val coverHash: String,
    @JsonNames("modified")
    @Serializable(with = OffsetDateTimeSerializer::class)
    val dateModified: LocalDateTime,
    @JsonNames("image")
    val imageUrl: String,
    @JsonNames("id")
    val issueId: Int,
    val series: Series,
    @JsonNames("issue")
    val name: String,
    val number: String,
) {
    @Serializable
    data class Series(
        val name: String,
        val volume: Int,
        val yearBegan: Int,
    )
}
