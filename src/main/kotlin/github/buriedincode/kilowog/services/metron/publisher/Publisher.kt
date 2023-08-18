package github.buriedincode.kilowog.services.metron.publisher

import github.buriedincode.kilowog.OffsetDateTimeSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.LocalDateTime

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Publisher(
    @JsonNames("cv_id")
    val comicvineId: Int? = null,
    @JsonNames("modified")
    @Serializable(with = OffsetDateTimeSerializer::class)
    val dateModified: LocalDateTime,
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
