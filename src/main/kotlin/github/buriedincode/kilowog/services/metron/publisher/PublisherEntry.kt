package github.buriedincode.kilowog.services.metron.publisher

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class PublisherEntry(
    @JsonNames("id")
    val publisherId: Int,
    val name: String,
    @JsonNames("modified")
    val dateModified: String,
)
