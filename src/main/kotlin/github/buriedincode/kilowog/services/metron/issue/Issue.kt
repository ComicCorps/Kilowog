package github.buriedincode.kilowog.services.metron.issue

import github.buriedincode.kilowog.OffsetDateTimeSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.LocalDateTime

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Issue(
    @JsonNames("cv_id")
    val comicvineId: Int? = null,
    val coverDate: LocalDate,
    val coverHash: String,
    val characters: List<DatedResource> = emptyList(),
    val credits: List<Credit> = emptyList(),
    @JsonNames("modified")
    @Serializable(with = OffsetDateTimeSerializer::class)
    val dateModified: LocalDateTime,
    @JsonNames("desc")
    val description: String? = null,
    @JsonNames("image")
    val imageUrl: String,
    val isbn: String? = null,
    @JsonNames("id")
    val issueId: Int,
    @JsonNames("name")
    val names: List<String> = emptyList(),
    val number: String,
    @JsonNames("page")
    val pageCount: Int? = 0,
    val price: String? = null,
    val publisher: Resource,
    val rating: Resource,
    val reprints: List<Reprint> = emptyList(),
    val resourceUrl: String,
    val series: Series,
    val sku: String? = null,
    val storeDate: LocalDate,
    @JsonNames("arcs")
    val storyArcs: List<DatedResource> = emptyList(),
    val teams: List<DatedResource> = emptyList(),
    val title: String? = null,
    val upc: String? = null,
    val variants: List<Variant> = emptyList(),
) {
    @Serializable
    data class Resource(
        val id: Int,
        val name: String,
    )

    @Serializable
    data class DatedResource(
        @JsonNames("modified")
        @Serializable(with = OffsetDateTimeSerializer::class)
        val dateModified: LocalDateTime,
        val id: Int,
        val name: String,
    )

    @Serializable
    data class Series(
        val genres: List<Resource> = emptyList(),
        val id: Int,
        val name: String,
        val seriesType: Resource,
        val sortName: String,
        val volume: Int,
    )

    @Serializable
    data class Credit(
        val creator: String,
        val id: Int,
        @JsonNames("role")
        val roles: List<Resource> = emptyList(),
    )

    @Serializable
    data class Reprint(
        val id: Int,
        @JsonNames("issue")
        val name: String,
    )

    @Serializable
    data class Variant(
        @JsonNames("image")
        val imageUrl: String,
        val name: String,
        val sku: String? = null,
        val upc: String? = null,
    )
}
