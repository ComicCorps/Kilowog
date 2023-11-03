package github.buriedincode.kilowog.models.metadata

import github.buriedincode.kilowog.Utils
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.apache.logging.log4j.kotlin.Logging

@Serializable
class Series(
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
    fun getFilename(): String = Utils.sanitize(if (volume == 1) title else "$title v$volume")

    override fun compareTo(other: Series): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Series

        if (format != other.format) return false
        if (publisher != other.publisher) return false
        if (title != other.title) return false
        if (volume != other.volume) return false

        return true
    }

    override fun hashCode(): Int {
        var result = format.hashCode()
        result = 31 * result + publisher.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + volume
        return result
    }

    override fun toString(): String {
        return "Series(format=$format, publisher=$publisher, resources=$resources, startYear=$startYear, title='$title', volume=$volume)"
    }

    companion object : Logging {
        private val comparator = compareBy(Series::publisher)
            .thenBy(String.CASE_INSENSITIVE_ORDER, Series::title)
            .thenBy(Series::volume)
            .thenBy(Series::format)
    }
}
