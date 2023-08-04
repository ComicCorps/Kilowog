package github.buriedincode.kilowog.services.metron.issue

import com.fasterxml.jackson.annotation.JsonAlias

data class Issue(
    @JsonAlias("id")
    val issueId: Int,
    val publisher: Publisher,
    val series: Series,
    val number: String,
    val title: String? = null,
    @JsonAlias("name")
    val names: List<String> = emptyList(),
    @JsonAlias("cover_date")
    val coverDate: String,
    @JsonAlias("store_date")
    val storeDate: String,
    val price: String,
    val rating: Rating,
    val sku: String? = null,
    val isbn: String? = null,
    val upc: String? = null,
    val page: Int,
    @JsonAlias("desc")
    val description: String? = null,
    @JsonAlias("image")
    val imageUrl: String,
    @JsonAlias("arcs")
    val storyArcs: List<StoryArc> = emptyList(),
    val credits: List<Credit> = emptyList(),
    val characters: List<Character> = emptyList(),
    val teams: List<Team> = emptyList(),
    val reprints: List<Reprint> = emptyList(),
    val variants: List<Variant> = emptyList(),
    @JsonAlias("resource_url")
    val resourceUrl: String,
    @JsonAlias("modified")
    val dateModified: String,
) {
    data class Publisher(
        @JsonAlias("id")
        val publisherId: Int,
        val name: String,
    )

    data class Series(
        @JsonAlias("id")
        val seriesId: Int,
        val name: String,
        @JsonAlias("sort_name")
        val sortName: String,
        val volume: Int,
        @JsonAlias("series_type")
        val type: SeriesType,
        val genres: List<Genre> = emptyList(),
    ) {
        data class SeriesType(
            @JsonAlias("id")
            val seriesTypeId: Int,
            val name: String,
        )

        data class Genre(
            @JsonAlias("id")
            val genreId: Int,
            val name: String,
        )
    }

    data class Rating(
        @JsonAlias("id")
        val ratingId: Int,
        val name: String,
    )

    data class StoryArc(
        @JsonAlias("id")
        val storyArcId: Int,
        val name: String,
        @JsonAlias("modified")
        val dateModified: String,
    )

    data class Credit(
        @JsonAlias("id")
        val creditId: Int,
        val creator: String,
        @JsonAlias("role")
        val roles: List<Role> = emptyList(),
    ) {
        data class Role(
            @JsonAlias("id")
            val roleId: Int,
            val name: String,
        )
    }

    data class Character(
        @JsonAlias("id")
        val characterId: Int,
        val name: String,
        @JsonAlias("modified")
        val dateModified: String,
    )

    data class Team(
        @JsonAlias("id")
        val teamId: Int,
        val name: String,
        @JsonAlias("modified")
        val dateModified: String,
    )

    data class Reprint(
        @JsonAlias("id")
        val reprintId: Int,
        val name: String,
        @JsonAlias("modified")
        val dateModified: String,
    )

    data class Variant(
        val name: String,
        val sku: String? = null,
        val upc: String? = null,
        @JsonAlias("image")
        val imageUrl: String,
    )
}
