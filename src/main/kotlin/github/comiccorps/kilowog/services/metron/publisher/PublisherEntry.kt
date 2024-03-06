package github.comiccorps.kilowog.services.metron.publisher

import github.comiccorps.kilowog.OffsetDateTimeSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.LocalDateTime

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class PublisherEntry(
    @JsonNames("modified")
    @Serializable(with = OffsetDateTimeSerializer::class)
    val dateModified: LocalDateTime,
    val id: Long,
    val name: String,
) : Comparable<PublisherEntry> {
    override fun compareTo(other: PublisherEntry): Int = comparator.compare(this, other)

    companion object {
        private val comparator = compareBy(PublisherEntry::name)
    }
}
