package github.buriedincode.kilowog.services.metron.publisher

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Publisher(
    @JsonNames("id")
    val publisherId: Int,
    val name: String,
    val founded: Int,
    @JsonNames("desc")
    val description: String? = null,
    @JsonNames("image")
    val imageUrl: String,
    val resourceUrl: String,
    @JsonNames("modified")
    val dateModified: String,
)
