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
    @XmlSerialName("Title")
    val title: String? = null,
    @XmlSerialName("Series")
    val series: String? = null,
    @XmlSerialName("Number")
    val number: String? = null,
    @XmlSerialName("Count")
    val count: Int? = null,
    @XmlSerialName("Volume")
    val volume: Int? = null,
    @XmlSerialName("AlternateSeries")
    val alternateSeries: String? = null,
    @XmlSerialName("AlternateNumber")
    val alternateNumber: String? = null,
    @XmlSerialName("AlternateCount")
    val alternateCount: Int? = null,
    @XmlSerialName("Summary")
    val summary: String? = null,
    @XmlSerialName("Notes")
    val notes: String? = null,
    @XmlSerialName("Year")
    val year: Int? = null,
    @XmlSerialName("Month")
    val month: Int? = null,
    @XmlSerialName("Day")
    val day: Int? = null,
    @XmlSerialName("Writer")
    val writer: String? = null,
    @XmlSerialName("Penciller")
    val penciller: String? = null,
    @XmlSerialName("Inker")
    val inker: String? = null,
    @XmlSerialName("Colorist")
    val colorist: String? = null,
    @XmlSerialName("Letterer")
    val letterer: String? = null,
    @XmlSerialName("CoverArtist")
    val coverArtist: String? = null,
    @XmlSerialName("Editor")
    val editor: String? = null,
    @XmlSerialName("Publisher")
    val publisher: String? = null,
    @XmlSerialName("Imprint")
    val imprint: String? = null,
    @XmlSerialName("Genre")
    val genre: String? = null,
    @XmlSerialName("Web")
    val web: String? = null,
    @XmlSerialName("PageCount")
    val pageCount: Int = 0,
    @XmlSerialName("LanguageISO")
    val languageIso: String? = null,
    @XmlSerialName("Format")
    val format: String? = null,
    @XmlSerialName("BlackAndWhite")
    val blackAndWhite: YesNo = YesNo.UNKNOWN,
    @XmlSerialName("Manga")
    val manga: Manga = Manga.UNKNOWN,
    @XmlSerialName("Characters")
    val characters: String? = null,
    @XmlSerialName("Teams")
    val teams: String? = null,
    @XmlSerialName("Locations")
    val locations: String? = null,
    @XmlSerialName("ScanInformation")
    val scanInformation: String? = null,
    @XmlSerialName("StoryArc")
    val storyArc: String? = null,
    @XmlSerialName("SeriesGroup")
    val seriesGroup: String? = null,
    @XmlSerialName("AgeRating")
    val ageRating: AgeRating = AgeRating.UNKNOWN,
    @XmlSerialName("Pages")
    @XmlChildrenName("Page")
    val pages: List<Page> = emptyList(),
    @XmlSerialName("CommunityRating")
    val communityRating: Double? = null,
    @XmlSerialName("MainCharacterOrTeam")
    val mainCharacterOrTeam: String? = null,
    @XmlSerialName("Review")
    val review: String? = null,
) {
    @XmlSerialName("noNamespaceSchemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    @XmlElement(false)
    private val schemaUrl: String = "https://raw.githubusercontent.com/Buried-In-Code/Dex-Starr/main/schemas/ComicInfo.xsd"

    @Serializable
    data class Page(
        @XmlElement(false)
        @XmlSerialName("Image")
        val image: Int,
        @XmlElement(false)
        @XmlSerialName("Type")
        val type: PageType = PageType.STORY,
        @XmlElement(false)
        @XmlSerialName("DoublePage")
        val doublePage: Boolean = false,
        @XmlElement(false)
        @XmlSerialName("ImageSize")
        val imageSize: Long? = null,
        @XmlElement(false)
        @XmlSerialName("Key")
        val key: String? = null,
        @XmlElement(false)
        @XmlSerialName("Bookmark")
        val bookmark: String? = null,
        @XmlElement(false)
        @XmlSerialName("ImageWidth")
        val imageWidth: Int? = null,
        @XmlElement(false)
        @XmlSerialName("ImageHeight")
        val imageHeight: Int? = null,
    )

    fun toMetadata(): Metadata {
        return Metadata(
            issue = Metadata.Issue(
                publisher = Metadata.Issue.Publisher(
                    title = this.publisher ?: "Missing Publisher title",
                ),
                series = Metadata.Issue.Series(
                    title = this.series ?: "Missing Series title",
                ),
                number = number,
                title = title,
            ),
        )
    }
}
