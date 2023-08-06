package github.buriedincode.kilowog

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.serialization.XML
import org.apache.logging.log4j.kotlin.Logging
import java.io.IOException
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileVisitResult
import java.util.stream.Collectors

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
    val JSON_MAPPER: ObjectMapper = JsonMapper.builder()
        .addModule(JavaTimeModule())
        .addModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, true)
                .configure(KotlinFeature.NullToEmptyMap, true)
                .configure(KotlinFeature.NullIsSameAsDefault, true)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, true)
                .build(),
        )
        .build()

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

    inline fun <reified T : Enum<T>> T.titleCase(): String {
        return this.name.lowercase().split("_").joinToString(" ") {
            it.replaceFirstChar(Char::uppercaseChar)
        }
    }

    internal fun listFiles(path: Path, fileExtension: String): List<Path> {
        require(Files.isDirectory(path)) { "Path must be a directory" }
        var results: List<Path> = emptyList()
        try {
            Files.walk(path, FileVisitOption.FOLLOW_LINKS).use { walk ->
                results = walk.filter { Files.isRegularFile(it) }
                    .filter { it.fileName.toString().endsWith(fileExtension) }
                    .collect(Collectors.toList())
            }
        } catch (ioe: IOException) {
            logger.warn("Unable to walk folders", ioe)
        }
        return results
    }

    @Throws(IOException::class)
    fun recursiveDeleteOnExit(path: Path) {
        Files.walkFileTree(
            path,
            object: SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    file.toFile().deleteOnExit()
                    return FileVisitResult.CONTINUE
                }
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    dir.toFile().deleteOnExit()
                    return FileVisitResult.CONTINUE
                }
            },
        )
    }
}
