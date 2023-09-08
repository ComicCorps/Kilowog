package github.buriedincode.kilowog.models

import github.buriedincode.kilowog.Utils
import github.buriedincode.kilowog.Utils.asEnumOrNull
import github.buriedincode.kilowog.Utils.titleCase
import github.buriedincode.kilowog.models.comicinfo.AgeRating
import github.buriedincode.kilowog.models.comicinfo.Manga
import github.buriedincode.kilowog.models.comicinfo.PageType
import github.buriedincode.kilowog.models.comicinfo.YesNo
import github.buriedincode.kilowog.models.metadata.Format
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import java.nio.file.Path
import kotlin.io.path.writeText

@Serializable
data class ComicInfo(
    @XmlSerialName("AgeRating")
    val ageRating: AgeRating = AgeRating.UNKNOWN,
    @XmlSerialName("AlternateCount")
    val alternateCount: Int? = null,
    @XmlSerialName("AlternateNumber")
    val alternateNumber: String? = null,
    @XmlSerialName("AlternateSeries")
    var alternateSeries: String? = null,
    @XmlSerialName("BlackAndWhite")
    val blackAndWhite: YesNo = YesNo.UNKNOWN,
    @XmlSerialName("Characters")
    var characters: String? = null,
    @XmlSerialName("Colorist")
    var colourists: String? = null,
    @XmlSerialName("CommunityRating")
    val communityRating: Double? = null,
    @XmlSerialName("Count")
    val count: Int? = null,
    @XmlSerialName("CoverArtist")
    var coverArtists: String? = null,
    @XmlSerialName("Day")
    var day: Int? = null,
    @XmlSerialName("Editor")
    var editors: String? = null,
    @XmlSerialName("Format")
    val format: String? = null,
    @XmlSerialName("Genre")
    var genres: String? = null,
    @XmlSerialName("Imprint")
    val imprint: String? = null,
    @XmlSerialName("Inker")
    var inkers: String? = null,
    @XmlSerialName("LanguageISO")
    val language: String? = null,
    @XmlSerialName("Letterer")
    var letterers: String? = null,
    @XmlSerialName("Locations")
    var locations: String? = null,
    @XmlSerialName("MainCharacterOrTeam")
    val mainCharacterOrTeam: String? = null,
    @XmlSerialName("Manga")
    val manga: Manga = Manga.UNKNOWN,
    @XmlSerialName("Month")
    var month: Int? = null,
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
    var pencillers: String? = null,
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
    var storyArcs: String? = null,
    @XmlSerialName("Summary")
    val summary: String? = null,
    @XmlSerialName("Teams")
    var teams: String? = null,
    @XmlSerialName("Title")
    val title: String? = null,
    @XmlSerialName("Volume")
    val volume: Int? = null,
    @XmlSerialName("Web")
    val web: String? = null,
    @XmlSerialName("Writer")
    var writers: String? = null,
    @XmlSerialName("Year")
    var year: Int? = null,
) {
    @XmlSerialName("noNamespaceSchemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    @XmlElement(false)
    private val schemaUrl: String = "https://raw.githubusercontent.com/Buried-In-Code/Dex-Starr/main/schemas/ComicInfo.xsd"

    @Serializable
    data class Page(
        @XmlElement(false)
        val bookmark: String? = null,
        @XmlElement(false)
        val doublePage: Boolean = false,
        @XmlElement(false)
        val image: Int? = null,
        @XmlElement(false)
        val imageHeight: Int? = null,
        @XmlElement(false)
        val imageSize: Long? = null,
        @XmlElement(false)
        val imageWidth: Int? = null,
        @XmlElement(false)
        val key: String? = null,
        @XmlElement(false)
        val type: PageType = PageType.STORY,
    )

    var characterList: List<String>
        get() = this.characters?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this.characters = value.joinToString(", ")
        }

    var colouristList: List<String>
        get() = this.colourists?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this.colourists = value.joinToString(", ")
        }

    var coverArtistList: List<String>
        get() = this.coverArtists?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this.coverArtists = value.joinToString(", ")
        }

    var coverDate: LocalDate?
        get() = this.year?.let {
            LocalDate(year = it, monthNumber = this.month ?: 1, dayOfMonth = this.day ?: 1)
        }
        set(value) {
            this.year = value?.year
            this.month = value?.monthNumber
            this.day = value?.dayOfMonth
        }

    val credits: Map<String, List<String>>
        get() {
            val output = mutableMapOf<String, MutableList<String>>()
            this.writerList.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableListOf()
                }
                output[it]?.add("Writer")
            }
            this.pencillerList.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableListOf()
                }
                output[it]?.add("Penciller")
            }
            this.inkerList.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableListOf()
                }
                output[it]?.add("Inker")
            }
            this.colouristList.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableListOf()
                }
                output[it]?.add("Colourist")
            }
            this.lettererList.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableListOf()
                }
                output[it]?.add("Letterer")
            }
            this.coverArtistList.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableListOf()
                }
                output[it]?.add("Cover Artist")
            }
            this.editorList.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableListOf()
                }
                output[it]?.add("Editor")
            }
            return output
        }

    var editorList: List<String>
        get() = this.editors?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this.editors = value.joinToString(", ")
        }

    var genreList: List<String>
        get() = this.genres?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this.genres = value.joinToString(", ")
        }

    var inkerList: List<String>
        get() = this.inkers?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this.inkers = value.joinToString(", ")
        }

    var lettererList: List<String>
        get() = this.letterers?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this.letterers = value.joinToString(", ")
        }

    var locationList: List<String>
        get() = this.locations?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this.locations = value.joinToString(", ")
        }

    var pencillerList: List<String>
        get() = this.pencillers?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this.pencillers = value.joinToString(", ")
        }

    var storyArcList: List<String>
        get() {
            val output: MutableList<String> = this.storyArcs?.split(",")?.map { it.trim() }?.toMutableList() ?: mutableListOf()
            output.addAll(this.alternateSeries?.split(",")?.map { it.trim() } ?: emptyList())
            return output
        }
        set(value) {
            this.storyArcs = value.joinToString(", ")
            this.alternateSeries = null
        }

    var teamList: List<String>
        get() = this.teams?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this.teams = value.joinToString(", ")
        }

    var writerList: List<String>
        get() = this.writers?.split(",")?.map { it.trim() } ?: emptyList()
        set(value) {
            this.writers = value.joinToString(", ")
        }

    fun toMetadata(): Metadata {
        return Metadata(
            issue = Metadata.Issue(
                characters = this.characterList.map {
                    Metadata.Issue.NamedResource(name = it.trim())
                },
                coverDate = this.coverDate,
                credits = this.credits.map { (key, value) ->
                    Metadata.Issue.Credit(
                        creator = Metadata.Issue.NamedResource(name = key),
                        roles = value,
                    )
                },
                genres = this.genreList,
                language = this.language ?: "en",
                locations = this.locationList.map {
                    Metadata.Issue.NamedResource(name = it)
                },
                number = this.number,
                pageCount = this.pageCount,
                // Missing Resources
                series = Metadata.Issue.Series(
                    format = this.format?.asEnumOrNull<Format>() ?: Format.COMIC,
                    publisher = Metadata.Issue.Series.Publisher(
                        imprint = this.imprint,
                        // Missing Resources
                        title = this.publisher ?: "Missing Publisher title",
                    ),
                    // Missing Resources
                    startYear = if (this.volume != null && this.volume >= 1900) this.volume else null,
                    title = this.series ?: "Missing Series title",
                    volume = if (this.volume != null && this.volume <= 1900) this.volume else 1,
                ),
                // Missing Store Date
                storyArcs = this.storyArcList.map {
                    Metadata.Issue.StoryArc(title = it)
                },
                summary = this.summary,
                teams = this.teams?.split(",")?.map {
                    Metadata.Issue.NamedResource(name = it)
                } ?: emptyList(),
                title = this.title,
            ),
            notes = this.notes,
            pages = this.pages.mapNotNull {
                Metadata.Page(
                    doublePage = it.doublePage,
                    filename = "Missing Page filename",
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
}
