package github.buriedincode.kilowog.metroninfo

import github.buriedincode.kilowog.metroninfo.enums.AgeRating
import github.buriedincode.kilowog.metroninfo.enums.Format
import github.buriedincode.kilowog.metroninfo.enums.Genre
import github.buriedincode.kilowog.metroninfo.enums.InformationSource
import github.buriedincode.kilowog.metroninfo.enums.PageType
import github.buriedincode.kilowog.metroninfo.enums.Role
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
data class MetronInfo(
    @XmlSerialName("ID")
    val id: Source? = null,
    @XmlSerialName("Publisher")
    val publisher: Resource,
    @XmlSerialName("Series")
    val series: Series,
    @XmlSerialName("CollectionTitle")
    val collectionTitle: String? = null,
    @XmlSerialName("Name")
    val number: String? = null,
    @XmlSerialName("Stories")
    @XmlChildrenName("Story")
    val stories: List<Resource> = emptyList(),
    @XmlSerialName("Summary")
    val summary: String? = null,
    @XmlSerialName("Prices")
    @XmlChildrenName("Price")
    val prices: List<Price> = emptyList(),
    @XmlSerialName("CoverDate")
    val coverDate: LocalDate,
    @XmlSerialName("StoreDate")
    val storeDate: LocalDate? = null,
    @XmlSerialName("PageCount")
    val pageCount: Int = 0,
    @XmlSerialName("Notes")
    val notes: String? = null,
    @XmlSerialName("Genres")
    @XmlChildrenName("Genre")
    val genres: List<GenreResource> = emptyList(),
    @XmlSerialName("Tags")
    @XmlChildrenName("Tag")
    val tags: List<Resource> = emptyList(),
    @XmlSerialName("Arcs")
    @XmlChildrenName("Arc")
    val arcs: List<Arc> = emptyList(),
    @XmlSerialName("Characters")
    @XmlChildrenName("Character")
    val characters: List<Resource> = emptyList(),
    @XmlSerialName("Teams")
    @XmlChildrenName("Team")
    val teams: List<Resource> = emptyList(),
    @XmlSerialName("Locations")
    @XmlChildrenName("Location")
    val locations: List<Resource> = emptyList(),
    @XmlSerialName("Reprints")
    @XmlChildrenName("Reprint")
    val reprints: List<Resource> = emptyList(),
    @XmlSerialName("GTIN")
    val gtin: Gtin? = null,
    @XmlSerialName("BlackAndWhite")
    val blackAndWhite: Boolean = false,
    @XmlSerialName("AgeRating")
    val ageRating: AgeRating = AgeRating.UNKNOWN,
    @XmlSerialName("URL")
    val url: String? = null,
    @XmlSerialName("Credits")
    @XmlChildrenName("Credit")
    val credits: List<Credit> = emptyList(),
    @XmlSerialName("Pages")
    @XmlChildrenName("Page")
    val pages: List<Page> = emptyList(),
) {
    @XmlSerialName("noNamespaceSchemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    @XmlElement(false)
    private val schemaUrl: String = "https://raw.githubusercontent.com/Metron-Project/metroninfo/master/drafts/v1.0/MetronInfo.xsd"

    @Serializable
    data class Source(
        @XmlElement(false)
        val source: InformationSource,
        @XmlValue
        val value: Int,
    )

    @Serializable
    data class Resource(
        @XmlElement(false)
        val id: Int,
        @XmlValue
        val value: String,
    )

    @Serializable
    data class Series(
        @XmlElement(false)
        val lang: String = "en",
        @XmlElement(false)
        val id: Int,
        @XmlSerialName("Name")
        val name: String,
        @XmlSerialName("SortName")
        val sortName: String? = null,
        @XmlSerialName("Volume")
        val volume: Int? = null,
        @XmlSerialName("Format")
        val format: Format? = null,
    )

    @Serializable
    data class Price(
        @XmlElement(false)
        val country: String,
        @XmlValue
        val value: Double,
    )

    @Serializable
    data class GenreResource(
        @XmlElement(false)
        val id: Int,
        @XmlValue
        val value: Genre,
    )

    @Serializable
    data class Arc(
        @XmlElement(false)
        val id: Int,
        @XmlSerialName("Name")
        val name: String,
        @XmlSerialName("Number")
        val number: Int? = null,
    )

    @Serializable
    data class Gtin(
        @XmlSerialName("ISBN")
        val isbn: String? = null,
        @XmlSerialName("UPC")
        val upc: String? = null,
    )

    @Serializable
    data class Credit(
        @XmlSerialName("Creator")
        val creator: Resource,
        @XmlSerialName("Roles")
        @XmlChildrenName("Role")
        val roles: List<RoleResource> = emptyList(),
    )

    @Serializable
    data class RoleResource(
        @XmlElement(false)
        val id: Int,
        @XmlValue
        val value: Role,
    )

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
        TODO()
    }
}
