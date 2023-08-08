package github.buriedincode.kilowog.services.comicvine.publisher

import github.buriedincode.kilowog.services.comicvine.GenericEntry
import github.buriedincode.kilowog.services.comicvine.Image
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Publisher(
    val aliases: String? = null,
    @JsonNames("api_detail_url")
    val apiUrl: String,
    val dateAdded: String,
    val dateLastUpdated: String,
    val description: String? = null,
    @JsonNames("id")
    val publisherId: Int,
    val image: Image,
    val locationAddress: String? = null,
    val locationCity: String? = null,
    val locationState: String? = null,
    val name: String,
    @JsonNames("site_detail_url")
    val siteUrl: String,
    @JsonNames("deck")
    val summary: String? = null,
    val characters: List<GenericEntry> = emptyList(),
    val storyArcs: List<GenericEntry> = emptyList(),
    val teams: List<GenericEntry> = emptyList(),
    val volumes: List<GenericEntry> = emptyList(),
)
