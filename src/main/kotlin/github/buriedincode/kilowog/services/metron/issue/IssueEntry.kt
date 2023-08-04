package github.buriedincode.kilowog.services.metron.issue

import com.fasterxml.jackson.annotation.JsonAlias

data class IssueEntry(
    @JsonAlias("id")
    val issueId: Int,
    @JsonAlias("issue")
    val name: String,
    @JsonAlias("cover_date")
    val coverDate: String,
    @JsonAlias("modified")
    val dateModified: String,
)
