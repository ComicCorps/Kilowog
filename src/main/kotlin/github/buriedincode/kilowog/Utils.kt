package github.buriedincode.kilowog

import com.sksamuel.hoplite.Secret
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.serialization.XML
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.div
import kotlin.io.path.extension
import kotlin.io.walkTopDown

object Utils {
    private val HOME_ROOT = Paths.get(System.getProperty("user.home"))
    internal const val VERSION = "0.1.1"
    internal val CACHE_ROOT = HOME_ROOT / ".cache" / "kilowog"
    internal val CONFIG_ROOT = HOME_ROOT / ".config" / "kilowog"
    internal val DATA_ROOT = HOME_ROOT / ".local" / "share" / "kilowog"

    val XML_MAPPER: XML = XML {
        indent = 4
        xmlDeclMode = XmlDeclMode.Charset
        jacksonPolicy()
    }

    @OptIn(ExperimentalSerializationApi::class)
    val JSON_MAPPER: Json = Json {
        prettyPrint = true
        encodeDefaults = true
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    init {
        if (!Files.exists(CACHE_ROOT)) {
            CACHE_ROOT.toFile().mkdirs()
        }
        if (!Files.exists(CONFIG_ROOT)) {
            CONFIG_ROOT.toFile().mkdirs()
        }
        if (!Files.exists(DATA_ROOT)) {
            DATA_ROOT.toFile().mkdirs()
        }
    }

    inline fun <reified T : Enum<T>> String.asEnumOrNull(): T? {
        return enumValues<T>().firstOrNull { it.name.replace("_", " ").equals(this.replace("_", " "), ignoreCase = true) }
    }

    inline fun <reified T : Enum<T>> T.titleCase(): String {
        return this.name.lowercase().split("_").joinToString(" ") {
            it.replaceFirstChar(Char::uppercaseChar)
        }
    }

    fun Secret?.isNullOrBlank(): Boolean = this?.value.isNullOrBlank()

    internal fun listFiles(
        path: Path,
        fileExtension: String? = null,
    ): List<Path> {
        require(Files.isDirectory(path)) { "Path must be a directory" }
        var results = path.toFile().walkTopDown().onEnter {
            !it.name.startsWith(".")
        }.filter { it.isFile }.map { it.toPath() }.toList()
        if (!fileExtension.isNullOrBlank()) {
            results = results.filter { it.extension == fileExtension }
        }
        return results
    }

    fun sanitize(value: String): String {
        var output: String = value.replace("[\\\\/:*?\"<>|]+".toRegex(), "")
        output = output.replace("-", " ")
        output = output.split(" ").filterNot { it.isBlank() }.joinToString(" ") { it.trim() }
        return output.replace(" ", "-")
    }
}
