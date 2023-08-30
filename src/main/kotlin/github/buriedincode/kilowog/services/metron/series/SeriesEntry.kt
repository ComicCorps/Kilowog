package github.buriedincode.kilowog.services.metron.series

import github.buriedincode.kilowog.OffsetDateTimeSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.LocalDateTime

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SeriesEntry(
    @JsonNames("modified")
    @Serializable(with = OffsetDateTimeSerializer::class)
    val dateModified: LocalDateTime,
    var issueCount: Int,
    @JsonNames("series")
    var name: String,
    @JsonNames("id")
    val seriesId: Int,
    var yearBegan: Int,
) : Comparable<SeriesEntry> {
    companion object {
        private val comparator = compareBy(SeriesEntry::name).thenBy { it.yearBegan }
    }

    override fun compareTo(other: SeriesEntry): Int = comparator.compare(this, other)
}
