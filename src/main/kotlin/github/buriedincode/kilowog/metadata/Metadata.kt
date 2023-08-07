package github.buriedincode.kilowog.metadata

import github.buriedincode.kilowog.Utils
import github.buriedincode.kilowog.metadata.enums.Source
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
data class Metadata(
    @XmlSerialName("Data")
    var issue: Issue,
    @XmlSerialName("Pages")
    @XmlChildrenName("Page")
    var pages: List<Page> = emptyList(),
    @XmlSerialName("Notes")
    var notes: String? = null,
    @XmlSerialName("Meta")
    var meta: Meta = Meta(),
) {
    @XmlSerialName("noNamespaceSchemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    @XmlElement(false)
    private val schemaUrl: String = "https://raw.githubusercontent.com/Buried-In-Code/Kilowog/main/schemas/Metadata.xsd"

    @Serializable
    data class Issue(
        @XmlSerialName("Publisher")
        var publisher: Publisher,
        @XmlSerialName("Series")
        var series: Series,
        @XmlSerialName("Number")
        var number: String? = null,
        @XmlSerialName("Title")
        var title: String? = null,
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
        @XmlSerialName("PageCount")
        var pageCount: Int = 0,
        @XmlSerialName("Resources")
        @XmlChildrenName("Resource")
        var resources: List<Resource> = emptyList(),
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
    ) {

        @Serializable
        data class Publisher(
            @XmlSerialName("Imprint")
            var imprint: String? = null,
            @XmlSerialName("Resources")
            @XmlChildrenName("Resource")
            var resources: List<Resource> = emptyList(),
            @XmlSerialName("Title")
            var title: String,
        ) {
            fun getFilename(): String = Utils.sanitize(if (imprint == null) title else "$title ($imprint)")
        }

        @Serializable
        data class Resource(
            @XmlElement(false)
            var source: Source,
            @XmlValue
            var value: Int,
        )

        @Serializable
        data class Series(
            @XmlSerialName("Format")
            var format: String = "Comic",
            @XmlSerialName("Resources")
            @XmlChildrenName("Resource")
            var resources: List<Resource> = emptyList(),
            @XmlSerialName("StartYear")
            var startYear: Int? = null,
            @XmlSerialName("Title")
            var title: String,
            @XmlSerialName("Volume")
            var volume: Int = 1,
        ) {
            fun getFilename(): String {
                var output = if (volume == 1) title else "$title v$volume"
                output += "_($format)"
                return Utils.sanitize(output)
            }
        }

        @Serializable
        data class NamedResource(
            @XmlSerialName("Name")
            var name: String,
            @XmlSerialName("Resources")
            @XmlChildrenName("Resource")
            var resources: List<Resource> = emptyList(),
        )

        @Serializable
        data class Credit(
            @XmlSerialName("Creator")
            var creator: NamedResource,
            @XmlSerialName("Roles")
            @XmlChildrenName("Role")
            var roles: List<String> = emptyList(),
        )

        @Serializable
        data class StoryArc(
            @XmlSerialName("Title")
            var title: String,
            @XmlSerialName("Number")
            var number: Int? = null,
            @XmlSerialName("Resources")
            @XmlChildrenName("Resource")
            var resources: List<Resource> = emptyList(),
        )

        fun getFilename(): String {
            val volumeTitle = if (series.volume == 1) series.title else "$series.title v${series.volume}"
            val output = if (number != null) "_#$number" else if (title != null) "_$title" else ""
            return Utils.sanitize(volumeTitle + output)
        }
    }

    @Serializable
    data class Page(
        @XmlElement(false)
        var doublePage: Boolean = false,
        @XmlElement(false)
        var fileSize: Long = 0L,
        @XmlElement(false)
        var filename: String,
        @XmlElement(false)
        var imageHeight: Int = 0,
        @XmlElement(false)
        var imageWidth: Int = 0,
        @XmlElement(false)
        var index: Int,
        @XmlElement(false)
        var type: String = "Story",
    )

    @Serializable
    data class Meta(
        @XmlElement(false)
        val date: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
        @XmlSerialName("Tool")
        val tool: Tool = Tool(),
    )

    @Serializable
    data class Tool(
        @XmlElement(false)
        val version: String = Utils.VERSION,
        @XmlValue
        val value: String = "Dex-Starr",
    )
}
