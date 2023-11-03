package github.buriedincode.kilowog.models.metadata

import github.buriedincode.kilowog.Utils
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
data class Tool(
    @XmlValue
    val value: String = "Kilowog",
    @XmlElement(false)
    val version: String = Utils.VERSION,
)
