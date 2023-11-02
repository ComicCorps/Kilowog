package github.buriedincode.kilowog.models

import github.buriedincode.kilowog.Utils
import github.buriedincode.kilowog.Utils.asEnumOrNull
import github.buriedincode.kilowog.Utils.titleCase
import github.buriedincode.kilowog.models.comicinfo.PageType
import github.buriedincode.kilowog.models.metadata.Format
import github.buriedincode.kilowog.models.metadata.Source
import github.buriedincode.kilowog.models.metroninfo.Genre
import github.buriedincode.kilowog.models.metroninfo.InformationSource
import github.buriedincode.kilowog.models.metroninfo.Role
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue
import org.apache.logging.log4j.kotlin.Logging
import java.nio.file.Path
import kotlin.io.path.writeText

@Serializable
data class Metadata(
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

    @Serializable
    data class Issue(
        @XmlSerialName("Characters")
        @XmlChildrenName("Character")
        var characters: List<NamedResource> = emptyList(),
        @XmlSerialName("CoverDate")
        var coverDate: LocalDate? = null,
        @XmlSerialName("Credits")
        @XmlChildrenName("Credit")
        var credits: List<Credit> = emptyList(),
        @XmlSerialName("Genres")
        @XmlChildrenName("Genre")
        var genres: List<String> = emptyList(),
        @XmlElement(false)
        var language: String = "en",
        @XmlSerialName("Locations")
        @XmlChildrenName("Location")
        var locations: List<NamedResource> = emptyList(),
        @XmlSerialName("Number")
        var number: String? = null,
        @XmlSerialName("PageCount")
        var pageCount: Int = 0,
        @XmlSerialName("Resources")
        @XmlChildrenName("Resource")
        var resources: List<Resource> = emptyList(),
        @XmlSerialName("Series")
        var series: Series,
        @XmlSerialName("StoreDate")
        var storeDate: LocalDate? = null,
        @XmlSerialName("StoryArcs")
        @XmlChildrenName("StoryArc")
        var storyArcs: List<StoryArc> = emptyList(),
        @XmlSerialName("Summary")
        var summary: String? = null,
        @XmlSerialName("Teams")
        @XmlChildrenName("Team")
        var teams: List<NamedResource> = emptyList(),
        @XmlSerialName("Title")
        var title: String? = null,
    ) : Comparable<Issue> {
        @Serializable
        class Resource(
            @XmlElement(false)
            var source: Source,
            @XmlValue
            var value: Long,
        ) : Comparable<Resource> {
            override fun compareTo(other: Resource): Int = comparator.compare(this, other)

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Resource) return false

                return source == other.source
            }

            override fun hashCode(): Int {
                return source.hashCode()
            }

            override fun toString(): String {
                return "Resource(source=$source, value=$value)"
            }

            companion object : Logging {
                private val comparator = compareBy(Resource::source)
            }
        }

        @Serializable
        data class Series(
            @XmlSerialName("Format")
            var format: Format = Format.COMIC,
            @XmlSerialName("Publisher")
            var publisher: Publisher,
            @XmlSerialName("Resources")
            @XmlChildrenName("Resource")
            var resources: List<Resource> = emptyList(),
            @XmlSerialName("StartYear")
            var startYear: Int? = null,
            @XmlSerialName("Title")
            var title: String,
            @XmlSerialName("Volume")
            var volume: Int = 1,
        ) : Comparable<Series> {
            @Serializable
            data class Publisher(
                @XmlSerialName("Imprint")
                var imprint: String? = null,
                @XmlSerialName("Resources")
                @XmlChildrenName("Resource")
                var resources: List<Resource> = emptyList(),
                @XmlSerialName("Title")
                var title: String,
            ) : Comparable<Publisher> {
                fun getFilename(): String = Utils.sanitize(if (imprint == null) title else "$title ($imprint)")

                override fun compareTo(other: Publisher): Int = comparator.compare(this, other)

                companion object : Logging {
                    private val comparator = compareBy(Publisher::title)
                        .thenBy(nullsLast(), Publisher::imprint)
                }
            }

            fun getFilename(): String = Utils.sanitize(if (volume == 1) title else "$title v$volume")

            override fun compareTo(other: Series): Int = comparator.compare(this, other)

            companion object : Logging {
                private val comparator = compareBy(Series::publisher)
                    .thenBy(Series::title)
                    .thenBy(Series::volume)
                    .thenBy(Series::format)
            }
        }

        @Serializable
        class NamedResource(
            @XmlSerialName("Name")
            var name: String,
            @XmlSerialName("Resources")
            @XmlChildrenName("Resource")
            var resources: List<Resource> = emptyList(),
        ) : Comparable<NamedResource> {
            override fun compareTo(other: NamedResource): Int = comparator.compare(this, other)

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is NamedResource) return false

                return name == other.name
            }

            override fun hashCode(): Int {
                return name.hashCode()
            }

            override fun toString(): String {
                return "NamedResource(name=$name, resources=$resources)"
            }

            companion object : Logging {
                private val comparator = compareBy(NamedResource::name)
            }
        }

        @Serializable
        data class Credit(
            @XmlSerialName("Creator")
            var creator: NamedResource,
            @XmlSerialName("Roles")
            @XmlChildrenName("Role")
            var roles: List<String> = emptyList(),
        ) : Comparable<Credit> {
            override fun compareTo(other: Credit): Int = comparator.compare(this, other)

            companion object : Logging {
                private val comparator = compareBy(Credit::creator)
            }
        }

        @Serializable
        data class StoryArc(
            @XmlSerialName("Number")
            var number: Int? = null,
            @XmlSerialName("Resources")
            @XmlChildrenName("Resource")
            var resources: List<Resource> = emptyList(),
            @XmlSerialName("Title")
            var title: String,
        ) : Comparable<StoryArc> {
            override fun compareTo(other: StoryArc): Int = comparator.compare(this, other)

            companion object : Logging {
                private val comparator = compareBy(StoryArc::title)
                    .thenBy(nullsLast(), StoryArc::number)
            }
        }

        fun getFilename(): String {
            val seriesTitle = if (series.volume == 1) series.title else "${series.title} v${series.volume}"
            val issueTitle = if (number != null) {
                "_#${number!!.padStart(if (series.format == Format.COMIC) 3 else 2, '0')}"
            } else if (title != null) {
                "_$title"
            } else {
                ""
            }
            val issueFormat = when (series.format) {
                Format.ANNUAL -> "_Annual"
                Format.DIGITAL_CHAPTER -> "_Chapter"
                Format.GRAPHIC_NOVEL -> "_GN"
                Format.HARDCOVER -> "_HC"
                Format.TRADE_PAPERBACK -> "_TP"
                else -> ""
            }
            return Utils.sanitize(
                value = when (series.format) {
                    Format.ANNUAL, Format.DIGITAL_CHAPTER -> seriesTitle + issueFormat + issueTitle
                    Format.GRAPHIC_NOVEL, Format.HARDCOVER, Format.TRADE_PAPERBACK -> seriesTitle + issueTitle + issueFormat
                    else -> seriesTitle + issueTitle
                },
            )
        }

        override fun compareTo(other: Issue): Int = comparator.compare(this, other)

        companion object : Logging {
            private val comparator = compareBy(Issue::series)
                .thenBy(nullsLast()) { it.number?.toIntOrNull() }
                .thenBy(Issue::number)
                .thenBy(nullsLast(), Issue::title)
        }
    }

    @Serializable
    data class Page(
        @XmlElement(false)
        var doublePage: Boolean = false,
        @XmlElement(false)
        var filename: String,
        @XmlElement(false)
        var fileSize: Long = 0L,
        @XmlElement(false)
        var imageHeight: Int = 0,
        @XmlElement(false)
        var imageWidth: Int = 0,
        @XmlElement(false)
        var index: Int,
        @XmlElement(false)
        var type: String = "Story",
    ) : Comparable<Page> {
        override fun compareTo(other: Page): Int = comparator.compare(this, other)

        companion object : Logging {
            private val comparator = compareBy(Page::index)
        }
    }

    @Serializable
    data class Meta(
        @XmlElement(false)
        val date: LocalDate? = null,
        @XmlSerialName("Tool")
        val tool: Tool = Tool(),
    )

    @Serializable
    data class Tool(
        @XmlValue
        val value: String = "Kilowog",
        @XmlElement(false)
        val version: String = Utils.VERSION,
    )

    fun toComicInfo(): ComicInfo {
        return ComicInfo(
            format = this.issue.series.format.titleCase(),
            imprint = this.issue.series.publisher.imprint,
            language = this.issue.language,
            notes = this.notes,
            number = this.issue.number,
            pageCount = this.issue.pageCount,
            pages = this.pages.map {
                ComicInfo.Page(
                    doublePage = it.doublePage,
                    image = it.index,
                    imageHeight = it.imageHeight,
                    imageSize = it.fileSize,
                    imageWidth = it.imageWidth,
                    type = it.type.asEnumOrNull<PageType>()
                        ?: PageType.STORY,
                )
            },
            publisher = this.issue.series.publisher.title,
            series = this.issue.series.title,
            summary = this.issue.summary,
            title = this.issue.title,
            volume = this.issue.series.volume,
        ).apply {
            this.characterList = this@Metadata.issue.characters.map { it.name }
            this.colouristList = this@Metadata.issue.credits
                .filter { "colorist" in it.roles.map { it.lowercase() } || "colourist" in it.roles.map { it.lowercase() } }
                .map { it.creator.name }
            this.coverArtistList = this@Metadata.issue.credits
                .filter { "cover artist" in it.roles.map { it.lowercase() } }
                .map { it.creator.name }
            this.coverDate = this@Metadata.issue.coverDate
            this.editorList = this@Metadata.issue.credits
                .filter { "editor" in it.roles.map { it.lowercase() } }
                .map { it.creator.name }
            this.genreList = this@Metadata.issue.genres
            this.inkerList = this@Metadata.issue.credits
                .filter { "inker" in it.roles.map { it.lowercase() } }
                .map { it.creator.name }
            this.lettererList = this@Metadata.issue.credits
                .filter { "letterer" in it.roles.map { it.lowercase() } }
                .map { it.creator.name }
            this.locationList = this@Metadata.issue.locations.map { it.name }
            this.pencillerList = this@Metadata.issue.credits
                .filter { "penciller" in it.roles.map { it.lowercase() } }
                .map { it.creator.name }
            this.storyArcList = this@Metadata.issue.storyArcs.map { it.title }
            this.teamList = this@Metadata.issue.teams.map { it.name }
            this.writerList = this@Metadata.issue.credits
                .filter { "writer" in it.roles.map { it.lowercase() } }
                .map { it.creator.name }
        }
    }

    fun toMetronInfo(): MetronInfo? {
        val source = this.issue.resources.firstOrNull { it.source == Source.METRON }?.source
            ?: this.issue.resources.firstOrNull { it.source == Source.COMICVINE }?.source
        return MetronInfo(
            arcs = this.issue.storyArcs.map {
                MetronInfo.Arc(
                    id = it.resources.firstOrNull { it.source == source }?.value,
                    name = it.title,
                    number = it.number,
                )
            },
            characters = this.issue.characters.map {
                MetronInfo.Resource(
                    id = it.resources.firstOrNull { it.source == source }?.value,
                    value = it.name,
                )
            },
            coverDate = this.issue.coverDate ?: return null,
            credits = this.issue.credits.map {
                MetronInfo.Credit(
                    creator = MetronInfo.Resource(
                        id = it.creator.resources.firstOrNull { it.source == source }?.value,
                        value = it.creator.name,
                    ),
                    roles = it.roles.mapNotNull {
                        MetronInfo.RoleResource(
                            value = it.asEnumOrNull<Role>() ?: return@mapNotNull null,
                        )
                    },
                )
            },
            genres = this.issue.genres.mapNotNull {
                MetronInfo.GenreResource(
                    value = it.asEnumOrNull<Genre>() ?: return@mapNotNull null,
                )
            },
            id = if (source?.titleCase()?.asEnumOrNull<InformationSource>() != null) {
                MetronInfo.Source(
                    source = source.titleCase().asEnumOrNull<InformationSource>()!!,
                    value = this.issue.resources.firstOrNull { it.source == source }?.value ?: return null,
                )
            } else {
                null
            },
            locations = this.issue.locations.map {
                MetronInfo.Resource(
                    id = it.resources.firstOrNull { it.source == source }?.value,
                    value = it.name,
                )
            },
            notes = this.notes,
            number = this.issue.number,
            pageCount = this.issue.pageCount,
            pages = this.pages.map {
                MetronInfo.Page(
                    doublePage = it.doublePage,
                    image = it.index,
                    imageHeight = it.imageHeight,
                    imageSize = it.fileSize,
                    imageWidth = it.imageWidth,
                    type = it.type.asEnumOrNull<github.buriedincode.kilowog.models.metroninfo.PageType>()
                        ?: github.buriedincode.kilowog.models.metroninfo.PageType.STORY,
                )
            },
            publisher = MetronInfo.Resource(
                id = this.issue.series.publisher.resources.firstOrNull { it.source == source }?.value,
                value = this.issue.series.publisher.imprint ?: this.issue.series.publisher.title,
            ),
            series = MetronInfo.Series(
                format = this.issue.series.format.titleCase().asEnumOrNull<github.buriedincode.kilowog.models.metroninfo.Format>(),
                id = this.issue.series.resources.firstOrNull { it.source == source }?.value,
                lang = this.issue.language,
                name = this.issue.series.title,
                volume = this.issue.series.volume,
            ),
            storeDate = this.issue.storeDate,
            summary = this.issue.summary,
            teams = this.issue.teams.map {
                MetronInfo.Resource(
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

    companion object : Logging {
        private val comparator = compareBy(Metadata::issue)
    }
}
