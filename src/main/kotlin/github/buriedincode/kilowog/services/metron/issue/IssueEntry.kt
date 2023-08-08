package github.buriedincode.kilowog.services.metron.issue

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class IssueEntry(
    @JsonNames("id")
    val issueId: Int,
    @JsonNames("issue")
    val name: String,
    val coverDate: String,
    @JsonNames("modified")
    val dateModified: String,
)
