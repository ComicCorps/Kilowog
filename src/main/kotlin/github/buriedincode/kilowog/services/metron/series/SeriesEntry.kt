package github.buriedincode.kilowog.services.metron.series

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SeriesEntry(
    @JsonNames("id")
    val seriesId: Int,
    @JsonNames("series")
    var name: String,
    var yearBegan: Int,
    var issueCount: Int,
    @JsonNames("modified")
    var dateModified: String,
)
