package github.comiccorps.kilowog.services.metron.publisher

import github.comiccorps.kilowog.OffsetDateTimeSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.LocalDateTime

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Publisher(
    @JsonNames("cv_id")
    val comicvineId: Long? = null,
    @JsonNames("modified")
    @Serializable(with = OffsetDateTimeSerializer::class)
    val dateModified: LocalDateTime,
    @JsonNames("desc")
    val description: String? = null,
    val founded: Int,
    val id: Long,
    @JsonNames("image")
    val imageUrl: String,
    val name: String,
    val resourceUrl: String,
)
