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
    val id: Long,
    var issueCount: Int,
    @JsonNames("series")
    var name: String,
    var yearBegan: Int,
) : Comparable<SeriesEntry> {
    override fun compareTo(other: SeriesEntry): Int = comparator.compare(this, other)

    companion object {
        private val comparator = compareBy(SeriesEntry::name)
            .thenBy { it.yearBegan }
    }
}
