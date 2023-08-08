package github.buriedincode.kilowog.services.comicvine.volume

import github.buriedincode.kilowog.services.comicvine.GenericEntry
import github.buriedincode.kilowog.services.comicvine.Image
import github.buriedincode.kilowog.services.comicvine.IssueEntry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class VolumeEntry(
    val aliases: String? = null,
    @JsonNames("api_detail_url")
    val apiUrl: String,
    val dateAdded: String,
    val dateLastUpdated: String,
    val description: String? = null,
    val firstIssue: IssueEntry? = null,
    @JsonNames("id")
    val volumeId: Int,
    val image: Image,
    @JsonNames("count_of_issues")
    val issueCount: Int,
    val lastIssue: IssueEntry? = null,
    val name: String,
    val publisher: GenericEntry? = null,
    @JsonNames("site_detail_url")
    val siteUrl: String,
    val startYear: Int,
    @JsonNames("deck")
    val summary: String? = null,
)
