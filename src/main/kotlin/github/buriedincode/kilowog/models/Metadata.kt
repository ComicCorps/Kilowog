package github.buriedincode.kilowog.models

import github.buriedincode.kilowog.Utils
import github.buriedincode.kilowog.Utils.asEnumOrNull
import github.buriedincode.kilowog.Utils.titleCase
import github.buriedincode.kilowog.models.metadata.Issue
import github.buriedincode.kilowog.models.metadata.Meta
import github.buriedincode.kilowog.models.metadata.Page
import github.buriedincode.kilowog.models.metadata.Source
import github.buriedincode.kilowog.models.metroninfo.Arc
import github.buriedincode.kilowog.models.metroninfo.Format
import github.buriedincode.kilowog.models.metroninfo.Genre
import github.buriedincode.kilowog.models.metroninfo.GenreResource
import github.buriedincode.kilowog.models.metroninfo.InformationSource
import github.buriedincode.kilowog.models.metroninfo.Role
import github.buriedincode.kilowog.models.metroninfo.RoleResource
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.apache.logging.log4j.kotlin.Logging
import java.nio.file.Path
import kotlin.io.path.writeText
import github.buriedincode.kilowog.models.comicinfo.Page as ComicPage
import github.buriedincode.kilowog.models.comicinfo.PageType as ComicPageType
import github.buriedincode.kilowog.models.metroninfo.Credit as MetronCredit
import github.buriedincode.kilowog.models.metroninfo.Page as MetronPage
import github.buriedincode.kilowog.models.metroninfo.PageType as MetronPageType
import github.buriedincode.kilowog.models.metroninfo.Resource as MetronResource
import github.buriedincode.kilowog.models.metroninfo.Series as MetronSeries
import github.buriedincode.kilowog.models.metroninfo.Source as MetronSource

@Serializable
class Metadata(
    @XmlSerialName("Issue")
    var issue: Issue,
    @XmlSerialName("Meta")
    var meta: Meta = Meta(),
    @XmlSerialName("Notes")
    var notes: String? = null,
    @XmlSerialName("Pages")
    @XmlChildrenName("Page")
    var pages: List<Page> = emptyList(),
) : Comparable<Metadata> {
    @XmlSerialName("noNamespaceSchemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    @XmlElement(false)
    private val schemaUrl: String = "https://raw.githubusercontent.com/Buried-In-Code/Kilowog/main/schemas/Metadata.xsd"

    fun toComicInfo(): ComicInfo {
        return ComicInfo(
            format = this.issue.series.format.titleCase(),
            imprint = this.issue.series.publisher.imprint,
            language = this.issue.language,
            notes = this.notes,
            number = this.issue.number,
            pageCount = this.issue.pageCount,
            pages = this.pages.map {
                ComicPage(
                    doublePage = it.doublePage,
                    image = it.index,
                    imageHeight = it.imageHeight,
                    imageSize = it.fileSize,
                    imageWidth = it.imageWidth,
                    type = it.type.asEnumOrNull<ComicPageType>() ?: ComicPageType.STORY,
                )
            },
            publisher = this.issue.series.publisher.title,
            series = this.issue.series.title,
            summary = this.issue.summary,
            title = this.issue.title,
            volume = this.issue.series.volume,
        ).apply {
            this.characters = this@Metadata.issue.characters.map { it.name }
            this.colourists = this@Metadata.issue.credits
                .filter { "colorist" in it.roles.map { it.lowercase() } || "colourist" in it.roles.map { it.lowercase() } }
                .map { it.creator.name }
            this.coverArtists = this@Metadata.issue.credits
                .filter { "cover artist" in it.roles.map { it.lowercase() } || "cover" in it.roles.map { it.lowercase() } }
                .map { it.creator.name }
            this.coverDate = this@Metadata.issue.coverDate
            this.editors = this@Metadata.issue.credits
                .filter { "editor" in it.roles.map { it.lowercase() } }
                .map { it.creator.name }
            this.genres = this@Metadata.issue.genres
            this.inkers = this@Metadata.issue.credits
                .filter { "inker" in it.roles.map { it.lowercase() } }
                .map { it.creator.name }
            this.letterers = this@Metadata.issue.credits
                .filter { "letterer" in it.roles.map { it.lowercase() } }
                .map { it.creator.name }
            this.locations = this@Metadata.issue.locations.map { it.name }
            this.pencillers = this@Metadata.issue.credits
                .filter { "penciller" in it.roles.map { it.lowercase() } }
                .map { it.creator.name }
            this.storyArcs = this@Metadata.issue.storyArcs.map { it.title }
            this.teams = this@Metadata.issue.teams.map { it.name }
            this.writers = this@Metadata.issue.credits
                .filter { "writer" in it.roles.map { it.lowercase() } }
                .map { it.creator.name }
        }
    }

    fun toMetronInfo(): MetronInfo? {
        val source = this.issue.resources.firstOrNull { it.source == Source.METRON }?.source
            ?: this.issue.resources.firstOrNull { it.source == Source.COMICVINE }?.source
        return MetronInfo(
            arcs = this.issue.storyArcs.map {
                Arc(
                    id = it.resources.firstOrNull { it.source == source }?.value,
                    name = it.title,
                    number = it.number,
                )
            },
            characters = this.issue.characters.map {
                MetronResource(
                    id = it.resources.firstOrNull { it.source == source }?.value,
                    value = it.name,
                )
            },
            coverDate = this.issue.coverDate ?: return null,
            credits = this.issue.credits.map {
                MetronCredit(
                    creator = MetronResource(
                        id = it.creator.resources.firstOrNull { it.source == source }?.value,
                        value = it.creator.name,
                    ),
                    roles = it.roles.mapNotNull {
                        RoleResource(
                            value = it.asEnumOrNull<Role>() ?: return@mapNotNull null,
                        )
                    },
                )
            },
            genres = this.issue.genres.mapNotNull {
                GenreResource(
                    value = it.asEnumOrNull<Genre>() ?: return@mapNotNull null,
                )
            },
            id = source?.titleCase()?.asEnumOrNull<InformationSource>()?.let {
                MetronSource(
                    source = it,
                    value = this.issue.resources.firstOrNull { it.source == source }?.value ?: return@let null,
                )
            },
            locations = this.issue.locations.map {
                MetronResource(
                    id = it.resources.firstOrNull { it.source == source }?.value,
                    value = it.name,
                )
            },
            notes = this.notes,
            number = this.issue.number,
            pageCount = this.issue.pageCount,
            pages = this.pages.map {
                MetronPage(
                    doublePage = it.doublePage,
                    image = it.index,
                    imageHeight = it.imageHeight,
                    imageSize = it.fileSize,
                    imageWidth = it.imageWidth,
                    type = it.type.asEnumOrNull<MetronPageType>() ?: MetronPageType.STORY,
                )
            },
            publisher = MetronResource(
                id = this.issue.series.publisher.resources.firstOrNull { it.source == source }?.value,
                value = this.issue.series.publisher.imprint ?: this.issue.series.publisher.title,
            ),
            series = MetronSeries(
                format = this.issue.series.format.titleCase().asEnumOrNull<Format>() ?: Format.SERIES,
                id = this.issue.series.resources.firstOrNull { it.source == source }?.value,
                lang = this.issue.language,
                name = this.issue.series.title,
                volume = this.issue.series.volume,
            ),
            storeDate = this.issue.storeDate,
            summary = this.issue.summary,
            teams = this.issue.teams.map {
                MetronResource(
                    id = it.resources.firstOrNull { it.source == source }?.value,
                    value = it.name,
                )
            },
            title = this.issue.title,
        )
    }

    fun toFile(file: Path) {
        val stringXml = Utils.XML_MAPPER.encodeToString(this)
        file.writeText(stringXml, charset = Charsets.UTF_8)
    }

    override fun compareTo(other: Metadata): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Metadata

        return issue == other.issue
    }

    override fun hashCode(): Int {
        return issue.hashCode()
    }

    override fun toString(): String {
        return "Metadata(issue=$issue, meta=$meta, notes=$notes, pages=$pages)"
    }

    companion object : Logging {
        private val comparator = compareBy(Metadata::issue)
    }
}
