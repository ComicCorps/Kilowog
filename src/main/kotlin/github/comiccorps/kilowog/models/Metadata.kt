package github.comiccorps.kilowog.models

import github.comiccorps.kilowog.Utils
import github.comiccorps.kilowog.Utils.asEnumOrNull
import github.comiccorps.kilowog.Utils.titlecase
import github.comiccorps.kilowog.models.metadata.Issue
import github.comiccorps.kilowog.models.metadata.Meta
import github.comiccorps.kilowog.models.metadata.Page
import github.comiccorps.kilowog.models.metadata.Source
import github.comiccorps.kilowog.models.metroninfo.Arc
import github.comiccorps.kilowog.models.metroninfo.Format
import github.comiccorps.kilowog.models.metroninfo.Genre
import github.comiccorps.kilowog.models.metroninfo.GenreResource
import github.comiccorps.kilowog.models.metroninfo.InformationSource
import github.comiccorps.kilowog.models.metroninfo.Role
import github.comiccorps.kilowog.models.metroninfo.RoleResource
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.apache.logging.log4j.kotlin.Logging
import java.nio.file.Path
import kotlin.io.path.writeText
import github.comiccorps.kilowog.models.comicinfo.Page as ComicPage
import github.comiccorps.kilowog.models.comicinfo.PageType as ComicPageType
import github.comiccorps.kilowog.models.metroninfo.Credit as MetronCredit
import github.comiccorps.kilowog.models.metroninfo.Page as MetronPage
import github.comiccorps.kilowog.models.metroninfo.PageType as MetronPageType
import github.comiccorps.kilowog.models.metroninfo.Resource as MetronResource
import github.comiccorps.kilowog.models.metroninfo.Series as MetronSeries
import github.comiccorps.kilowog.models.metroninfo.Source as MetronSource

@Serializable
class Metadata(
    @XmlSerialName("Issue")
    var issue: Issue,
    @XmlSerialName("Meta")
    var meta: Meta,
    @XmlSerialName("Notes")
    var notes: String? = null,
    @XmlSerialName("Pages")
    @XmlChildrenName("Page")
    var pages: List<Page> = emptyList(),
) : Comparable<Metadata> {
    @XmlSerialName("noNamespaceSchemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    @XmlElement(false)
    var schemaUrl: String = Metadata.schemaUrl

    fun toComicInfo(): ComicInfo {
        return ComicInfo(
            format = this.issue.format.titlecase(),
            language = this.issue.language,
            notes = this.notes,
            number = this.issue.number,
            pageCount = this.issue.pageCount,
            pages = this.pages.map {
                ComicPage(
                    doublePage = it.doublePage,
                    image = it.index,
                    imageHeight = it.height,
                    imageSize = it.size,
                    imageWidth = it.width,
                    type = it.type.name.asEnumOrNull<ComicPageType>() ?: ComicPageType.STORY,
                )
            },
            publisher = this.issue.series.publisher.title,
            series = this.issue.series.title,
            summary = this.issue.summary,
            title = this.issue.title,
            volume = this.issue.series.volume,
        ).apply {
            this.characters = this@Metadata.issue.characters.map { it.title }
            this.colourists = this@Metadata.issue.credits
                .filter { "colorist" in it.roles.map { it.title.lowercase() } || "colourist" in it.roles.map { it.title.lowercase() } }
                .map { it.creator.title }
            this.coverArtists = this@Metadata.issue.credits
                .filter { "cover artist" in it.roles.map { it.title.lowercase() } || "cover" in it.roles.map { it.title.lowercase() } }
                .map { it.creator.title }
            this.coverDate = this@Metadata.issue.coverDate
            this.editors = this@Metadata.issue.credits
                .filter { "editor" in it.roles.map { it.title.lowercase() } }
                .map { it.creator.title }
            this.genres = this@Metadata.issue.genres.map { it.title }
            this.inkers = this@Metadata.issue.credits
                .filter { "inker" in it.roles.map { it.title.lowercase() } }
                .map { it.creator.title }
            this.letterers = this@Metadata.issue.credits
                .filter { "letterer" in it.roles.map { it.title.lowercase() } }
                .map { it.creator.title }
            this.locations = this@Metadata.issue.locations.map { it.title }
            this.pencillers = this@Metadata.issue.credits
                .filter { "penciller" in it.roles.map { it.title.lowercase() } }
                .map { it.creator.title }
            this.storyArcs = this@Metadata.issue.storyArcs.map { it.title }
            this.teams = this@Metadata.issue.teams.map { it.title }
            this.writers = this@Metadata.issue.credits
                .filter { "writer" in it.roles.map { it.title.lowercase() } }
                .map { it.creator.title }
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
                    value = it.title,
                )
            },
            coverDate = this.issue.coverDate ?: return null,
            credits = this.issue.credits.map {
                MetronCredit(
                    creator = MetronResource(
                        id = it.creator.resources.firstOrNull { it.source == source }?.value,
                        value = it.creator.title,
                    ),
                    roles = it.roles.mapNotNull {
                        RoleResource(
                            id = it.resources.firstOrNull { it.source == source }?.value,
                            value = it.title.asEnumOrNull<Role>() ?: return@mapNotNull null,
                        )
                    },
                )
            },
            genres = this.issue.genres.mapNotNull {
                GenreResource(
                    id = it.resources.firstOrNull { it.source == source }?.value,
                    value = it.title.asEnumOrNull<Genre>() ?: return@mapNotNull null,
                )
            },
            id = source?.titlecase()?.asEnumOrNull<InformationSource>()?.let {
                MetronSource(
                    source = it,
                    value = this.issue.resources.firstOrNull { it.source == source }?.value ?: return@let null,
                )
            },
            locations = this.issue.locations.map {
                MetronResource(
                    id = it.resources.firstOrNull { it.source == source }?.value,
                    value = it.title,
                )
            },
            notes = this.notes,
            number = this.issue.number,
            pageCount = this.issue.pageCount,
            pages = this.pages.map {
                MetronPage(
                    doublePage = it.doublePage,
                    image = it.index,
                    imageHeight = it.height,
                    imageSize = it.size,
                    imageWidth = it.width,
                    type = it.type.name.asEnumOrNull<MetronPageType>() ?: MetronPageType.STORY,
                )
            },
            publisher = MetronResource(
                id = this.issue.series.publisher.resources.firstOrNull { it.source == source }?.value,
                value = this.issue.series.publisher.title,
            ),
            series = MetronSeries(
                format = this.issue.format.titlecase().asEnumOrNull<Format>() ?: Format.SERIES,
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
                    value = it.title,
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
        val schemaUrl: String = "https://raw.githubusercontent.com/ComicCorps/Schemas/main/drafts/Metadata.xsd"

        private val comparator = compareBy(Metadata::issue)
    }
}
