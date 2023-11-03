package github.buriedincode.kilowog.models.metadata

import github.buriedincode.kilowog.Utils
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.apache.logging.log4j.kotlin.Logging

@Serializable
class Publisher(
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Publisher

        if (imprint != other.imprint) return false
        if (title != other.title) return false

        return true
    }

    override fun hashCode(): Int {
        var result = imprint?.hashCode() ?: 0
        result = 31 * result + title.hashCode()
        return result
    }

    override fun toString(): String {
        return "Publisher(imprint=$imprint, resources=$resources, title='$title')"
    }

    companion object : Logging {
        private val comparator = compareBy(String.CASE_INSENSITIVE_ORDER, Publisher::title)
            .thenBy(nullsFirst(), Publisher::imprint)
    }
}
