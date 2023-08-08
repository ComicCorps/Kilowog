package github.buriedincode.kilowog.services.metron.issue

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Issue(
    @JsonNames("id")
    val issueId: Int,
    val publisher: Publisher,
    val series: Series,
    val number: String,
    val title: String? = null,
    @JsonNames("name")
    val names: List<String> = emptyList(),
    val coverDate: String,
    val storeDate: String,
    val price: String,
    val rating: Rating,
    val sku: String? = null,
    val isbn: String? = null,
    val upc: String? = null,
    val page: Int,
    @JsonNames("desc")
    val description: String? = null,
    @JsonNames("image")
    val imageUrl: String,
    @JsonNames("arcs")
    val storyArcs: List<StoryArc> = emptyList(),
    val credits: List<Credit> = emptyList(),
    val characters: List<Character> = emptyList(),
    val teams: List<Team> = emptyList(),
    val reprints: List<Reprint> = emptyList(),
    val variants: List<Variant> = emptyList(),
    val resourceUrl: String,
    @JsonNames("modified")
    val dateModified: String,
) {
    @Serializable
    data class Publisher(
        @JsonNames("id")
        val publisherId: Int,
        val name: String,
    )

    @Serializable
    data class Series(
        @JsonNames("id")
        val seriesId: Int,
        val name: String,
        val sortName: String,
        val volume: Int,
        val seriesType: SeriesType,
        val genres: List<Genre> = emptyList(),
    ) {
        @Serializable
        data class SeriesType(
            @JsonNames("id")
            val seriesTypeId: Int,
            val name: String,
        )

        @Serializable
        data class Genre(
            @JsonNames("id")
            val genreId: Int,
            val name: String,
        )
    }

    @Serializable
    data class Rating(
        @JsonNames("id")
        val ratingId: Int,
        val name: String,
    )

    @Serializable
    data class StoryArc(
        @JsonNames("id")
        val storyArcId: Int,
        val name: String,
        @JsonNames("modified")
        val dateModified: String,
    )

    @Serializable
    data class Credit(
        @JsonNames("id")
        val creditId: Int,
        val creator: String,
        @JsonNames("role")
        val roles: List<Role> = emptyList(),
    ) {
        @Serializable
        data class Role(
            @JsonNames("id")
            val roleId: Int,
            val name: String,
        )
    }

    @Serializable
    data class Character(
        @JsonNames("id")
        val characterId: Int,
        val name: String,
        @JsonNames("modified")
        val dateModified: String,
    )

    @Serializable
    data class Team(
        @JsonNames("id")
        val teamId: Int,
        val name: String,
        @JsonNames("modified")
        val dateModified: String,
    )

    @Serializable
    data class Reprint(
        @JsonNames("id")
        val reprintId: Int,
        val name: String,
        @JsonNames("modified")
        val dateModified: String,
    )

    @Serializable
    data class Variant(
        val name: String,
        val sku: String? = null,
        val upc: String? = null,
        @JsonNames("image")
        val imageUrl: String,
    )
}
