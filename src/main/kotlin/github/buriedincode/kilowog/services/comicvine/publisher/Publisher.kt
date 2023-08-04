package github.buriedincode.kilowog.services.comicvine.publisher

import com.fasterxml.jackson.annotation.JsonAlias
import github.buriedincode.kilowog.services.comicvine.GenericEntry
import github.buriedincode.kilowog.services.comicvine.Image

data class Publisher(
    val aliases: String? = null,
    @JsonAlias("api_detail_url")
    val apiUrl: String,
    @JsonAlias("date_added")
    val dateAdded: String,
    @JsonAlias("date_last_updated")
    val dateLastUpdated: String,
    val description: String? = null,
    @JsonAlias("id")
    val publisherId: Int,
    val image: Image,
    @JsonAlias("location_address")
    val locationAddress: String? = null,
    @JsonAlias("location_city")
    val locationCity: String? = null,
    @JsonAlias("location_state")
    val locationState: String? = null,
    val name: String,
    @JsonAlias("site_detail_url")
    val siteUrl: String,
    @JsonAlias("deck")
    val summary: String? = null,
    val characters: List<GenericEntry> = emptyList(),
    @JsonAlias("story_arcs")
    val storyArcs: List<GenericEntry> = emptyList(),
    val teams: List<GenericEntry> = emptyList(),
    val volumes: List<GenericEntry> = emptyList(),
)
