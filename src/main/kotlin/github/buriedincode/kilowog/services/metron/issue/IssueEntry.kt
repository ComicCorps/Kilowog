package github.buriedincode.kilowog.services.metron.issue

import github.buriedincode.kilowog.OffsetDateTimeSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.apache.logging.log4j.kotlin.Logging
import java.time.LocalDateTime

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class IssueEntry(
    val coverDate: LocalDate,
    val coverHash: String,
    @JsonNames("modified")
    @Serializable(with = OffsetDateTimeSerializer::class)
    val dateModified: LocalDateTime,
    val id: Long,
    @JsonNames("image")
    val imageUrl: String,
    val series: Series,
    @JsonNames("issue")
    val name: String,
    val number: String,
) : Comparable<IssueEntry> {
    @Serializable
    data class Series(
        val name: String,
        val volume: Int,
        val yearBegan: Int,
    ) : Comparable<Series> {
        override fun compareTo(other: Series): Int = comparator.compare(this, other)

        companion object : Logging {
            private val comparator = compareBy(Series::name)
                .thenBy(Series::volume)
                .thenBy(Series::yearBegan)
        }
    }

    override fun compareTo(other: IssueEntry): Int = comparator.compare(this, other)

    companion object : Logging {
        private val comparator = compareBy(IssueEntry::series)
            .thenBy(nullsLast()) { it.number.toIntOrNull() }
            .thenBy(IssueEntry::number)
    }
}
