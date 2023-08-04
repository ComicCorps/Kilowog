package github.buriedincode.kilowog.services.comicvine.volume

import com.fasterxml.jackson.annotation.JsonAlias
import github.buriedincode.kilowog.services.comicvine.GenericEntry
import github.buriedincode.kilowog.services.comicvine.Image
import github.buriedincode.kilowog.services.comicvine.IssueEntry

data class VolumeEntry(
    val aliases: String? = null,
    @JsonAlias("api_detail_url")
    val apiUrl: String,
    @JsonAlias("date_added")
    val dateAdded: String,
    @JsonAlias("date_last_updated")
    val dateLastUpdated: String,
    val description: String? = null,
    @JsonAlias("first_issue")
    val firstIssue: IssueEntry? = null,
    @JsonAlias("id")
    val volumeId: Int,
    val image: Image,
    @JsonAlias("count_of_issues")
    val issueCount: Int,
    @JsonAlias("last_issue")
    val lastEntry: IssueEntry? = null,
    val name: String,
    val publisher: GenericEntry? = null,
    @JsonAlias("site_detail_url")
    val siteUrl: String,
    @JsonAlias("start_year")
    val startYear: Int,
    @JsonAlias("deck")
    val summary: String? = null,
)
