package github.buriedincode.kilowog.services.comicvine

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class AssociatedImage(
    val caption: String? = null,
    val id: Int,
    @JsonNames("image_tags")
    val tags: String,
    val originalUrl: String,
)