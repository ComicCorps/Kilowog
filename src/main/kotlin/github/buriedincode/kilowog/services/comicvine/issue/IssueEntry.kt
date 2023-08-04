package github.buriedincode.kilowog.services.comicvine.issue

import com.fasterxml.jackson.annotation.JsonAlias
import github.buriedincode.kilowog.services.comicvine.AlternativeImage
import github.buriedincode.kilowog.services.comicvine.GenericEntry
import github.buriedincode.kilowog.services.comicvine.Image

data class IssueEntry(
    val aliases: String? = null,
    @JsonAlias("associated_images")
    val alternativeImages: List<AlternativeImage> = emptyList(),
    @JsonAlias("api_detail_url")
    val apiUrl: String,
    @JsonAlias("cover_date")
    val coverDate: String? = null,
    @JsonAlias("date_added")
    val dateAdded: String,
    @JsonAlias("date_last_updated")
    val dateLastUpdated: String,
    val description: String? = null,
    @JsonAlias("has_staff_review")
    val hasStaffReview: Boolean,
    @JsonAlias("id")
    val issueId: Int,
    val image: Image,
    val name: String? = null,
    @JsonAlias("issue_number")
    val number: String? = null,
    @JsonAlias("site_detail_url")
    val siteUrl: String,
    @JsonAlias("store_date")
    val storeDate: String? = null,
    @JsonAlias("deck")
    val summary: String? = null,
    val volume: GenericEntry,
)
