package github.buriedincode.kilowog.comicinfo

import github.buriedincode.kilowog.comicinfo.enums.AgeRating
import github.buriedincode.kilowog.comicinfo.enums.Manga
import github.buriedincode.kilowog.comicinfo.enums.PageType
import github.buriedincode.kilowog.comicinfo.enums.YesNo
import github.buriedincode.kilowog.metadata.Metadata
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
data class ComicInfo(
    @XmlSerialName("AgeRating")
    val ageRating: AgeRating = AgeRating.UNKNOWN,
    @XmlSerialName("AlternateCount")
    val alternateCount: Int? = null,
    @XmlSerialName("AlternateNumber")
    val alternateNumber: String? = null,
    @XmlSerialName("AlternateSeries")
    val alternateSeries: String? = null,
    @XmlSerialName("BlackAndWhite")
    val blackAndWhite: YesNo = YesNo.UNKNOWN,
    @XmlSerialName("Characters")
    val characters: String? = null,
    @XmlSerialName("Colorist")
    val colourist: String? = null,
    @XmlSerialName("CommunityRating")
    val communityRating: Double? = null,
    @XmlSerialName("Count")
    val count: Int? = null,
    @XmlSerialName("CoverArtist")
    val coverArtist: String? = null,
    @XmlSerialName("Day")
    val day: Int? = null,
    @XmlSerialName("Editor")
    val editor: String? = null,
    @XmlSerialName("Format")
    val format: String? = null,
    @XmlSerialName("Genre")
    val genre: String? = null,
    @XmlSerialName("Imprint")
    val imprint: String? = null,
    @XmlSerialName("Inker")
    val inker: String? = null,
    @XmlSerialName("LanguageISO")
    val language: String? = null,
    @XmlSerialName("Letterer")
    val letterer: String? = null,
    @XmlSerialName("Locations")
    val locations: String? = null,
    @XmlSerialName("MainCharacterOrTeam")
    val mainCharacterOrTeam: String? = null,
    @XmlSerialName("Manga")
    val manga: Manga = Manga.UNKNOWN,
    @XmlSerialName("Month")
    val month: Int? = null,
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
    val penciller: String? = null,
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
    val storyArc: String? = null,
    @XmlSerialName("Summary")
    val summary: String? = null,
    @XmlSerialName("Teams")
    val teams: String? = null,
    @XmlSerialName("Title")
    val title: String? = null,
    @XmlSerialName("Volume")
    val volume: Int? = null,
    @XmlSerialName("Web")
    val web: String? = null,
    @XmlSerialName("Writer")
    val writer: String? = null,
    @XmlSerialName("Year")
    val year: Int? = null,
) {
    @XmlSerialName("noNamespaceSchemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    @XmlElement(false)
    private val schemaUrl: String = "https://raw.githubusercontent.com/Buried-In-Code/Dex-Starr/main/schemas/ComicInfo.xsd"

    @Serializable
    data class Page(
        @XmlElement(false)
        @XmlSerialName("Bookmark")
        val bookmark: String? = null,
        @XmlElement(false)
        @XmlSerialName("DoublePage")
        val doublePage: Boolean = false,
        @XmlElement(false)
        @XmlSerialName("Image")
        val image: Int,
        @XmlElement(false)
        @XmlSerialName("ImageHeight")
        val imageHeight: Int? = null,
        @XmlElement(false)
        @XmlSerialName("ImageSize")
        val imageSize: Long? = null,
        @XmlElement(false)
        @XmlSerialName("ImageWidth")
        val imageWidth: Int? = null,
        @XmlElement(false)
        @XmlSerialName("Key")
        val key: String? = null,
        @XmlElement(false)
        @XmlSerialName("Type")
        val type: PageType = PageType.STORY,
    )

    var coverDate: LocalDate?
        get() = this.year?.let {
            LocalDate(year = it, monthNumber = this.month ?: 1, dayOfMonth = this.day ?: 1)
        }
        set(value: LocalDate?) {
            this.year = value?.year
            this.month = value?.monthNumber
            this.day = value?.day
        }

    val credits: Map<String, List<String>>
        get() {
            val output = mutableMap<String, List<String>>()
            this.writer.split(",").map { it.trim() }.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableList<String>()
                }
                output[it].add("Writer")
            }
            this.penciller.split(",").map { it.trim() }.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableList<String>()
                }
                output[it].add("Penciller")
            }
            this.inker.split(",").map { it.trim() }.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableList<String>()
                }
                output[it].add("Inker")
            }
            this.colourist.split(",").map { it.trim() }.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableList<String>()
                }
                output[it].add("Colourist")
            }
            this.letterer.split(",").map { it.trim() }.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableList<String>()
                }
                output[it].add("Letterer")
            }
            this.coverArtist.split(",").map { it.trim() }.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableList<String>()
                }
                output[it].add("Cover Artist")
            }
            this.editor.split(",").map { it.trim() }.forEach {
                if (!output.contains(it)) {
                    output[it] = mutableList<String>()
                }
                output[it].add("Editor")
            }
            return output
        }

    fun toMetadata(): Metadata {
        return Metadata(
            issue = Metadata.Issue(
                characters = this.characters.split(',').map {
                    Metadata.Issue.NamedResource(name = it.trim())
                },
                coverDate = this.coverDate,
                credits = this.credits.map { (key, value) ->
                    Metadata.Issue.Credit(
                        creator = Metadata.Issue.NamedResource(name = key),
                        roles = value,
                    )
                },
                genres = this.genres.split(',').map {
                    it.trim()
                },
                language = this.language,
                locations = this.locations.split(',').map {
                    Metadata.Issue.NamedResource(name = it)
                },
                number = this.number,
                pageCount = this.pageCount,
                publisher = Metadata.Issue.Publisher(
                    imprint = this.imprint,
                    // Missing Resources
                    title = this.publisher ?: "Missing Publisher title",
                ),
                // Missing Resources
                series = Metadata.Issue.Series(
                    format = this.format ?: "Comic",
                    // Missing Resources
                    startYear = if (this.volume >= 1900) this.volume else None,
                    title = this.series ?: "Missing Series title",
                    volume = if (this.volume <= 1900) this.volume else 1,
                ),
                // Missing Store Date
                // Missing Story Arcs
                summary = this.summary,
                teams = this.teams.split(",").map {
                    Metadata.Issue.NamedResource(name = it)
                },
                title = this.title,
            ),
            notes = this.notes,
            pages = this.pages.map {
                Metadata.Page(
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
}
