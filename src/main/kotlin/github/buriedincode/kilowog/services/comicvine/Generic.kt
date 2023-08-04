package github.buriedincode.kilowog.services.comicvine

import com.fasterxml.jackson.annotation.JsonAlias

data class GenericEntry(
    @JsonAlias("api_detail_url")
    val apiUrl: String,
    val id: Int,
    val name: String? = null,
    @JsonAlias("site_detail_url")
    val siteUrl: String? = null,
)

data class CountEntry(
    @JsonAlias("api_detail_url")
    val apiUrl: String,
    val id: Int,
    val name: String? = null,
    @JsonAlias("site_detail_url")
    val siteUrl: String? = null,
    val count: Int,
)

data class CreatorEntry(
    @JsonAlias("api_detail_url")
    val apiUrl: String,
    val id: Int,
    val name: String? = null,
    @JsonAlias("site_detail_url")
    val siteUrl: String? = null,
    val role: String,
)

data class IssueEntry(
    @JsonAlias("api_detail_url")
    val apiUrl: String,
    val id: Int,
    val name: String? = null,
    @JsonAlias("site_detail_url")
    val siteUrl: String? = null,
    @JsonAlias("issue_number")
    val number: String? = null,
)
