package github.buriedincode.kilowog

import com.sksamuel.hoplite.Secret
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.serialization.XML
import org.apache.logging.log4j.kotlin.Logging
import java.io.IOException
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile

object Utils : Logging {
    internal const val VERSION = "0.0.0"
    internal val CACHE_ROOT = Paths.get(System.getProperty("user.home"), ".cache", "kilowog")
    internal val CONFIG_ROOT = Paths.get(System.getProperty("user.home"), ".config", "kilowog")
    internal val DATA_ROOT = Paths.get(System.getProperty("user.home"), ".local", "share", "kilowog")

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
            try {
                Files.createDirectories(CACHE_ROOT)
            } catch (ioe: IOException) {
                logger.error("Unable to create cache folder", ioe)
            }
        }
        if (!Files.exists(CONFIG_ROOT)) {
            try {
                Files.createDirectories(CONFIG_ROOT)
            } catch (ioe: IOException) {
                logger.error("Unable to create config folder", ioe)
            }
        }
        if (!Files.exists(DATA_ROOT)) {
            try {
                Files.createDirectories(DATA_ROOT)
            } catch (ioe: IOException) {
                logger.error("Unable to create data folder", ioe)
            }
        }
    }

    inline fun <reified T : Enum<T>> String.asEnumOrNull(): T? {
        return enumValues<T>().firstOrNull { it.name.equals(this, ignoreCase = true) }
    }

    inline fun <reified T : Enum<T>> T.titleCase(): String {
        return this.name.lowercase().split("_").joinToString(" ") {
            it.replaceFirstChar(Char::uppercaseChar)
        }
    }

    fun Secret?.isNullOrBlank(): Boolean = this?.value.isNullOrBlank()

    internal fun listFiles(path: Path, fileExtension: String? = null): List<Path> {
        require(Files.isDirectory(path)) { "Path must be a directory" }
        var results: List<Path> = emptyList()
        try {
            Files.walk(path, FileVisitOption.FOLLOW_LINKS).use {
                results = it.filter { it.isRegularFile() }.toList()
                if (!fileExtension.isNullOrBlank()) {
                    results = results.filter { it.extension == fileExtension }.toList()
                }
            }
        } catch (ioe: IOException) {
            logger.warn("Unable to walk folders", ioe)
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
