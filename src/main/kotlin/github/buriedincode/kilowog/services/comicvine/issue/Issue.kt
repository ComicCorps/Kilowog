package github.buriedincode.kilowog.services.comicvine.issue

import com.fasterxml.jackson.annotation.JsonAlias
import github.buriedincode.kilowog.services.comicvine.AlternativeImage
import github.buriedincode.kilowog.services.comicvine.CreatorEntry
import github.buriedincode.kilowog.services.comicvine.GenericEntry
import github.buriedincode.kilowog.services.comicvine.Image

data class Issue(
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
    @JsonAlias("character_credits")
    val characters: List<GenericEntry> = emptyList(),
    @JsonAlias("concept_credits")
    val concepts: List<GenericEntry> = emptyList(),
    @JsonAlias("person_credits")
    val creators: List<CreatorEntry> = emptyList(),
    @JsonAlias("character_died_in")
    val deaths: List<GenericEntry> = emptyList(),
    @JsonAlias("first_appearance_characters")
    val firstAppearanceCharacters: List<GenericEntry> = emptyList(),
    @JsonAlias("first_appearance_concepts")
    val firstAppearanceConcepts: List<GenericEntry> = emptyList(),
    @JsonAlias("first_appearance_locations")
    val firstAppearanceLocations: List<GenericEntry> = emptyList(),
    @JsonAlias("first_appearance_objects")
    val firstAppearanceObjects: List<GenericEntry> = emptyList(),
    @JsonAlias("first_appearance_storyarcs")
    val firstAppearanceStoryArcs: List<GenericEntry> = emptyList(),
    @JsonAlias("first_appearance_teams")
    val firstAppearanceTeams: List<GenericEntry> = emptyList(),
    @JsonAlias("location_credits")
    val locations: List<GenericEntry> = emptyList(),
    @JsonAlias("object_credits")
    val objects: List<GenericEntry> = emptyList(),
    @JsonAlias("story_arc_credits")
    val storyArcs: List<GenericEntry> = emptyList(),
    @JsonAlias("team_credits")
    val teams: List<GenericEntry> = emptyList(),
    @JsonAlias("team_disbanded_in")
    val teamsDisbanded: List<GenericEntry> = emptyList(),
)
