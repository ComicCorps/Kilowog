package github.buriedincode.kilowog.services.comicvine.issue

import github.buriedincode.kilowog.services.comicvine.AssociatedImage
import github.buriedincode.kilowog.services.comicvine.GenericEntry
import github.buriedincode.kilowog.services.comicvine.Image
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class IssueEntry(
    val aliases: String? = null,
    val associatedImages: List<AssociatedImage> = emptyList(),
    @JsonNames("api_detail_url")
    val apiUrl: String,
    val coverDate: String? = null,
    val dateAdded: String,
    val dateLastUpdated: String,
    val description: String? = null,
    val hasStaffReview: Boolean,
    @JsonNames("id")
    val issueId: Int,
    val image: Image,
    val name: String? = null,
    @JsonNames("issue_number")
    val number: String? = null,
    @JsonNames("site_detail_url")
    val siteUrl: String,
    val storeDate: String? = null,
    @JsonNames("deck")
    val summary: String? = null,
    val volume: GenericEntry,
)
