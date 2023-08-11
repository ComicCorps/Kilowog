package github.buriedincode.kilowog.services.metron.publisher

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Publisher(
    @JsonNames("cv_id")
    val comicvineId: Int? = null,
    @JsonNames("modified")
    val dateModified: String,
    @JsonNames("desc")
    val description: String? = null,
    val founded: Int,
    @JsonNames("image")
    val imageUrl: String,
    val name: String,
    @JsonNames("id")
    val publisherId: Int,
    val resourceUrl: String,
)
