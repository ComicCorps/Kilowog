package github.buriedincode.kilowog.models.metroninfo

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import org.apache.logging.log4j.kotlin.Logging

@Serializable
class Page(
    @XmlElement(false)
    val bookmark: String? = null,
    @XmlElement(false)
    val doublePage: Boolean = false,
    @XmlElement(false)
    val image: Int,
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
) : Comparable<Page> {
    override fun compareTo(other: Page): Int = comparator.compare(this, other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Page

        return image == other.image
    }

    override fun hashCode(): Int {
        return image
    }

    override fun toString(): String {
        return "Page(" +
            "bookmark=$bookmark, " +
            "doublePage=$doublePage, " +
            "image=$image, " +
            "imageHeight=$imageHeight, " +
            "imageSize=$imageSize, " +
            "imageWidth=$imageWidth, " +
            "key=$key, " +
            "type=$type" +
            ")"
    }

    companion object : Logging {
        private val comparator = compareBy(Page::image)
    }
}
