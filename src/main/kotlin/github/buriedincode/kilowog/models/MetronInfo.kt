package github.buriedincode.kilowog.models

import github.buriedincode.kilowog.Utils
import github.buriedincode.kilowog.Utils.asEnumOrNull
import github.buriedincode.kilowog.Utils.titleCase
import github.buriedincode.kilowog.models.metadata.Format
import github.buriedincode.kilowog.models.metadata.Issue
import github.buriedincode.kilowog.models.metadata.NamedResource
import github.buriedincode.kilowog.models.metadata.Publisher
import github.buriedincode.kilowog.models.metadata.StoryArc
import github.buriedincode.kilowog.models.metroninfo.AgeRating
import github.buriedincode.kilowog.models.metroninfo.Arc
import github.buriedincode.kilowog.models.metroninfo.Credit
import github.buriedincode.kilowog.models.metroninfo.GenreResource
import github.buriedincode.kilowog.models.metroninfo.Gtin
import github.buriedincode.kilowog.models.metroninfo.Page
import github.buriedincode.kilowog.models.metroninfo.Price
import github.buriedincode.kilowog.models.metroninfo.Resource
import github.buriedincode.kilowog.models.metroninfo.Series
import github.buriedincode.kilowog.models.metroninfo.Source
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import java.nio.file.Path
import kotlin.io.path.writeText
import github.buriedincode.kilowog.models.metadata.Credit as MetadataCredit
import github.buriedincode.kilowog.models.metadata.Page as MetadataPage
import github.buriedincode.kilowog.models.metadata.Resource as MetadataResource
import github.buriedincode.kilowog.models.metadata.Series as MetadataSeries
import github.buriedincode.kilowog.models.metadata.Source as MetadataSource

@Serializable
class MetronInfo(
    @XmlSerialName("AgeRating")
    val ageRating: AgeRating = AgeRating.UNKNOWN,
    @XmlSerialName("Arcs")
    @XmlChildrenName("Arc")
    val arcs: List<Arc> = emptyList(),
    @XmlSerialName("BlackAndWhite")
    val blackAndWhite: Boolean = false,
    @XmlSerialName("Characters")
    @XmlChildrenName("Character")
    val characters: List<Resource> = emptyList(),
    @XmlSerialName("CoverDate")
    val coverDate: LocalDate,
    @XmlSerialName("Credits")
    @XmlChildrenName("Credit")
    val credits: List<Credit> = emptyList(),
    @XmlSerialName("Genres")
    @XmlChildrenName("Genre")
    val genres: List<GenreResource> = emptyList(),
    @XmlSerialName("GTIN")
    val gtin: Gtin? = null,
    @XmlSerialName("ID")
    val id: Source? = null,
    @XmlSerialName("Locations")
    @XmlChildrenName("Location")
    val locations: List<Resource> = emptyList(),
    @XmlSerialName("Notes")
    val notes: String? = null,
    @XmlSerialName("Number")
    val number: String? = null,
    @XmlSerialName("PageCount")
    val pageCount: Int = 0,
    @XmlSerialName("Pages")
    @XmlChildrenName("Page")
    val pages: List<Page> = emptyList(),
    @XmlSerialName("Prices")
    @XmlChildrenName("Price")
    val prices: List<Price> = emptyList(),
    @XmlSerialName("Publisher")
    val publisher: Resource,
    @XmlSerialName("Reprints")
    @XmlChildrenName("Reprint")
    val reprints: List<Resource> = emptyList(),
    @XmlSerialName("Series")
    val series: Series,
    @XmlSerialName("StoreDate")
    val storeDate: LocalDate? = null,
    @XmlSerialName("Stories")
    @XmlChildrenName("Story")
    val stories: List<Resource> = emptyList(),
    @XmlSerialName("Summary")
    val summary: String? = null,
    @XmlSerialName("Tags")
    @XmlChildrenName("Tag")
    val tags: List<Resource> = emptyList(),
    @XmlSerialName("Teams")
    @XmlChildrenName("Team")
    val teams: List<Resource> = emptyList(),
    @XmlSerialName("CollectionTitle")
    val title: String? = null,
    @XmlSerialName("URL")
    val url: String? = null,
) {
    @XmlSerialName("noNamespaceSchemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    @XmlElement(false)
    private val schemaUrl: String = "https://raw.githubusercontent.com/Metron-Project/metroninfo/master/drafts/v1.0/MetronInfo.xsd"

    fun toMetadata(): Metadata {
        val source: MetadataSource? = id?.source?.name?.asEnumOrNull<MetadataSource>()
        return Metadata(
            issue = Issue(
                characters = this.characters.map { resource ->
                    NamedResource(
                        name = resource.value,
                        resources = listOfNotNull(
                            source?.let {
                                MetadataResource(source = it, value = resource.id ?: return@let null)
                            },
                        ),
                    )
                },
                coverDate = this.coverDate,
                credits = this.credits.map { credit ->
                    MetadataCredit(
                        creator = NamedResource(
                            name = credit.creator.value,
                            resources = listOfNotNull(
                                source?.let {
                                    MetadataResource(source = it, value = credit.creator.id ?: return@let null)
                                },
                            ),
                        ),
                        roles = credit.roles.map { it.value.titleCase() },
                    )
                },
                genres = this.genres.map {
                    it.value.titleCase()
                },
                language = this.series.lang,
                locations = this.locations.map { location ->
                    NamedResource(
                        name = location.value,
                        resources = listOfNotNull(
                            source?.let {
                                MetadataResource(source = it, value = location.id ?: return@let null)
                            },
                        ),
                    )
                },
                number = this.number,
                pageCount = this.pageCount,
                resources = listOfNotNull(
                    source?.let {
                        this.id?.let { id ->
                            MetadataResource(source = source, value = id.value)
                        }
                    },
                ),
                series = MetadataSeries(
                    format = this.series.format?.titleCase()?.asEnumOrNull<Format>()
                        ?: Format.COMIC,
                    publisher = Publisher(
                        resources = listOfNotNull(
                            source?.let {
                                MetadataResource(source = it, value = publisher.id ?: return@let null)
                            },
                        ),
                        title = this.publisher.value,
                    ),
                    resources = listOfNotNull(
                        source?.let {
                            MetadataResource(source = it, value = series.id ?: return@let null)
                        },
                    ),
                    title = this.series.name,
                    volume = this.series.volume ?: 1,
                ),
                storeDate = this.storeDate,
                storyArcs = this.arcs.map { arc ->
                    StoryArc(
                        number = arc.number,
                        resources = listOfNotNull(
                            source?.let {
                                MetadataResource(source = it, value = arc.id ?: return@let null)
                            },
                        ),
                        title = arc.name,
                    )
                },
                summary = this.summary,
                teams = this.teams.map { team ->
                    NamedResource(
                        name = team.value,
                        resources = listOfNotNull(
                            source?.let {
                                MetadataResource(source = it, value = team.id ?: return@let null)
                            },
                        ),
                    )
                },
                title = this.title,
            ),
            notes = this.notes,
            pages = this.pages.map {
                MetadataPage(
                    doublePage = it.doublePage,
                    filename = "",
                    fileSize = it.imageSize ?: 0L,
                    imageHeight = it.imageHeight ?: 0,
                    imageWidth = it.imageWidth ?: 0,
                    index = it.image,
                    type = it.type.titleCase(),
                )
            },
        )
    }

    fun toFile(file: Path) {
        val stringXml = Utils.XML_MAPPER.encodeToString(this)
        file.writeText(stringXml, charset = Charsets.UTF_8)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MetronInfo

        if (number != other.number) return false
        if (publisher != other.publisher) return false
        if (series != other.series) return false
        if (title != other.title) return false

        return true
    }

    override fun hashCode(): Int {
        var result = number?.hashCode() ?: 0
        result = 31 * result + publisher.hashCode()
        result = 31 * result + series.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MetronInfo(" +
            "ageRating=$ageRating, " +
            "arcs=$arcs, " +
            "blackAndWhite=$blackAndWhite, " +
            "characters=$characters, " +
            "coverDate=$coverDate, " +
            "credits=$credits, " +
            "genres=$genres, " +
            "gtin=$gtin, " +
            "id=$id, " +
            "locations=$locations, " +
            "notes=$notes, " +
            "number=$number, " +
            "pageCount=$pageCount, " +
            "pages=$pages, " +
            "prices=$prices, " +
            "publisher=$publisher, " +
            "reprints=$reprints, " +
            "series=$series, " +
            "storeDate=$storeDate, " +
            "stories=$stories, " +
            "summary=$summary, " +
            "tags=$tags, " +
            "teams=$teams, " +
            "title=$title, " +
            "url=$url, " +
            "schemaUrl='$schemaUrl'" +
            ")"
    }
}
