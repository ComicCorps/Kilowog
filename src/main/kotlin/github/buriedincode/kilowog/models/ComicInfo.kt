package github.buriedincode.kilowog.models

import github.buriedincode.kilowog.Utils
import github.buriedincode.kilowog.Utils.asEnumOrNull
import github.buriedincode.kilowog.Utils.titleCase
import github.buriedincode.kilowog.models.comicinfo.AgeRating
import github.buriedincode.kilowog.models.comicinfo.Manga
import github.buriedincode.kilowog.models.comicinfo.Page
import github.buriedincode.kilowog.models.comicinfo.YesNo
import github.buriedincode.kilowog.models.metadata.Credit
import github.buriedincode.kilowog.models.metadata.Format
import github.buriedincode.kilowog.models.metadata.Issue
import github.buriedincode.kilowog.models.metadata.NamedResource
import github.buriedincode.kilowog.models.metadata.Publisher
import github.buriedincode.kilowog.models.metadata.Series
import github.buriedincode.kilowog.models.metadata.StoryArc
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import java.nio.file.Path
import kotlin.io.path.writeText
import github.buriedincode.kilowog.models.metadata.Page as MetadataPage

@Serializable
class ComicInfo(
    @XmlSerialName("AgeRating")
    val ageRating: AgeRating = AgeRating.UNKNOWN,
    @XmlSerialName("AlternateCount")
    val alternateCount: Int? = null,
    @XmlSerialName("AlternateNumber")
    val alternateNumber: String? = null,
    @XmlSerialName("AlternateSeries")
    private var _alternateSeries: String? = null,
    @XmlSerialName("BlackAndWhite")
    val blackAndWhite: YesNo = YesNo.UNKNOWN,
    @XmlSerialName("Characters")
    private var _characters: String? = null,
    @XmlSerialName("Colorist")
    private var _colourist: String? = null,
    @XmlSerialName("CommunityRating")
    val communityRating: Double? = null,
    @XmlSerialName("Count")
    val count: Int? = null,
    @XmlSerialName("CoverArtist")
    private var _coverArtist: String? = null,
    @XmlSerialName("Day")
    private var _day: Int? = null,
    @XmlSerialName("Editor")
    private var _editor: String? = null,
    @XmlSerialName("Format")
    val format: String? = null,
    @XmlSerialName("Genre")
    private var _genre: String? = null,
    @XmlSerialName("Imprint")
    val imprint: String? = null,
    @XmlSerialName("Inker")
    private var _inker: String? = null,
    @XmlSerialName("LanguageISO")
    val language: String? = null,
    @XmlSerialName("Letterer")
    private var _letterer: String? = null,
    @XmlSerialName("Locations")
    private var _locations: String? = null,
    @XmlSerialName("MainCharacterOrTeam")
    val mainCharacterOrTeam: String? = null,
    @XmlSerialName("Manga")
    val manga: Manga = Manga.UNKNOWN,
    @XmlSerialName("Month")
    private var _month: Int? = null,
    @XmlSerialName("Notes")
    val notes: String? = null,
    @XmlSerialName("Number")
    val number: String? = null,
    @XmlSerialName("PageCount")
    val pageCount: Int = 0,
    @XmlSerialName("Pages")
    @XmlChildrenName("Page")
    val pages: List<Page> = emptyList(),
    @XmlSerialName("Penciller")
    private var _penciller: String? = null,
    @XmlSerialName("Publisher")
    val publisher: String? = null,
    @XmlSerialName("Review")
    val review: String? = null,
    @XmlSerialName("ScanInformation")
    val scanInformation: String? = null,
    @XmlSerialName("Series")
    val series: String? = null,
    @XmlSerialName("SeriesGroup")
    val seriesGroup: String? = null,
    @XmlSerialName("StoryArc")
    private var _storyArc: String? = null,
    @XmlSerialName("Summary")
    val summary: String? = null,
    @XmlSerialName("Teams")
    private var _teams: String? = null,
    @XmlSerialName("Title")
    val title: String? = null,
    @XmlSerialName("Volume")
    val volume: Int? = null,
    @XmlSerialName("Web")
    val web: String? = null,
    @XmlSerialName("Writer")
    private var _writer: String? = null,
    @XmlSerialName("Year")
    private var _year: Int? = null,
) {
    @XmlSerialName("noNamespaceSchemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    @XmlElement(false)
    private val schemaUrl: String = "https://raw.githubusercontent.com/Buried-In-Code/Dex-Starr/main/schemas/ComicInfo.xsd"

    var characters: List<String>
        get() = this._characters?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this._characters = value.joinToString(", ")
        }

    var colourists: List<String>
        get() = this._colourist?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this._colourist = value.joinToString(", ")
        }

    var coverArtists: List<String>
        get() = this._coverArtist?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this._coverArtist = value.joinToString(", ")
        }

    var coverDate: LocalDate?
        get() = this._year?.let {
            LocalDate(year = it, monthNumber = this._month ?: 1, dayOfMonth = this._day ?: 1)
        }
        set(value) {
            this._year = value?.year
            this._month = value?.monthNumber
            this._day = value?.dayOfMonth
        }

    val credits: Map<String, List<String>>
        get() {
            val output = mutableMapOf<String, MutableList<String>>()
            this.colourists.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableListOf()
                }
                output[it]?.add("Colourist")
            }
            this.coverArtists.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableListOf()
                }
                output[it]?.add("Cover Artist")
            }
            this.editors.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableListOf()
                }
                output[it]?.add("Editor")
            }
            this.inkers.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableListOf()
                }
                output[it]?.add("Inker")
            }
            this.letterers.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableListOf()
                }
                output[it]?.add("Letterer")
            }
            this.pencillers.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableListOf()
                }
                output[it]?.add("Penciller")
            }
            this.writers.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableListOf()
                }
                output[it]?.add("Writer")
            }
            return output
        }

    var editors: List<String>
        get() = this._editor?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this._editor = value.joinToString(", ")
        }

    var genres: List<String>
        get() = this._genre?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this._genre = value.joinToString(", ")
        }

    var inkers: List<String>
        get() = this._inker?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this._inker = value.joinToString(", ")
        }

    var letterers: List<String>
        get() = this._letterer?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this._letterer = value.joinToString(", ")
        }

    var locations: List<String>
        get() = this._locations?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this._locations = value.joinToString(", ")
        }

    var pencillers: List<String>
        get() = this._penciller?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this._penciller = value.joinToString(", ")
        }

    var storyArcs: List<String>
        get() {
            val output: MutableList<String> = this._storyArc?.split(",")?.map { it.trim() }?.toMutableList() ?: mutableListOf()
            output.addAll(this._alternateSeries?.split(",")?.map { it.trim() } ?: emptyList())
            return output
        }
        set(value) {
            this._storyArc = value.joinToString(", ")
            this._alternateSeries = null
        }

    var teams: List<String>
        get() = this._teams?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this._teams = value.joinToString(", ")
        }

    var writers: List<String>
        get() = this._writer?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this._writer = value.joinToString(", ")
        }

    fun toMetadata(): Metadata? {
        return Metadata(
            issue = Issue(
                characters = this.characters.map {
                    NamedResource(name = it.trim())
                },
                coverDate = this.coverDate,
                credits = this.credits.map { (key, value) ->
                    Credit(
                        creator = NamedResource(name = key),
                        roles = value,
                    )
                },
                genres = this.genres,
                language = this.language ?: "en",
                locations = this.locations.map {
                    NamedResource(name = it)
                },
                number = this.number,
                pageCount = this.pageCount,
                // Missing Resources
                series = Series(
                    format = this.format?.asEnumOrNull<Format>() ?: Format.COMIC,
                    publisher = Publisher(
                        imprint = this.imprint,
                        // Missing Resources
                        title = this.publisher ?: return null,
                    ),
                    // Missing Resources
                    startYear = if (this.volume != null && this.volume >= 1900) this.volume else null,
                    title = this.series ?: return null,
                    volume = if (this.volume != null && this.volume <= 1900) this.volume else 1,
                ),
                // Missing Store Date
                storyArcs = this.storyArcs.map {
                    StoryArc(title = it)
                },
                summary = this.summary,
                teams = this._teams?.split(",")?.map {
                    NamedResource(name = it)
                } ?: emptyList(),
                title = this.title,
            ),
            notes = this.notes,
            pages = this.pages.mapNotNull {
                MetadataPage(
                    doublePage = it.doublePage,
                    filename = "",
                    fileSize = it.imageSize ?: 0L,
                    imageHeight = it.imageHeight ?: 0,
                    imageWidth = it.imageWidth ?: 0,
                    index = it.image ?: return@mapNotNull null,
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

        other as ComicInfo

        if (format != other.format) return false
        if (imprint != other.imprint) return false
        if (language != other.language) return false
        if (number != other.number) return false
        if (publisher != other.publisher) return false
        if (series != other.series) return false
        if (title != other.title) return false
        if (volume != other.volume) return false

        return true
    }

    override fun hashCode(): Int {
        var result = format?.hashCode() ?: 0
        result = 31 * result + (imprint?.hashCode() ?: 0)
        result = 31 * result + (language?.hashCode() ?: 0)
        result = 31 * result + (number?.hashCode() ?: 0)
        result = 31 * result + (publisher?.hashCode() ?: 0)
        result = 31 * result + (series?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (volume ?: 0)
        return result
    }

    override fun toString(): String {
        return "ComicInfo(" +
            "ageRating=$ageRating, " +
            "alternateCount=$alternateCount, " +
            "alternateNumber=$alternateNumber, " +
            "blackAndWhite=$blackAndWhite, " +
            "characters=$characters, " +
            "colourists=$colourists, " +
            "communityRating=$communityRating, " +
            "count=$count, " +
            "coverArtists=$coverArtists, " +
            "coverDate=$coverDate, " +
            "editors=$editors, " +
            "format=$format, " +
            "genres=$genres, " +
            "imprint=$imprint, " +
            "inkers=$inkers, " +
            "language=$language, " +
            "letterers=$letterers, " +
            "locations=$locations, " +
            "mainCharacterOrTeam=$mainCharacterOrTeam, " +
            "manga=$manga, " +
            "notes=$notes, " +
            "number=$number, " +
            "pageCount=$pageCount, " +
            "pages=$pages, " +
            "pencillers=$pencillers, " +
            "publisher=$publisher, " +
            "review=$review, " +
            "scanInformation=$scanInformation, " +
            "series=$series, " +
            "seriesGroup=$seriesGroup, " +
            "storyArcs=$storyArcs, " +
            "summary=$summary, " +
            "teams=$teams, " +
            "title=$title, " +
            "volume=$volume, " +
            "web=$web, " +
            "writers=$writers, " +
            ")"
    }
}
