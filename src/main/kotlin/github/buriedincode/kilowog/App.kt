package github.buriedincode.kilowog

import com.github.junrar.Junrar
import github.buriedincode.kilowog.Utils.isNullOrBlank
import github.buriedincode.kilowog.console.Console
import github.buriedincode.kilowog.models.ComicInfo
import github.buriedincode.kilowog.models.Metadata
import github.buriedincode.kilowog.models.MetronInfo
import github.buriedincode.kilowog.services.ComicvineTalker
import github.buriedincode.kilowog.services.MetronTalker
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.kotlin.Logging
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.div
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

object App : Logging {
    fun convertCollection(directory: Path) {
        Utils.listFiles(directory, "cbr").forEach { srcFile ->
            logger.info("Converting ${srcFile.name} to CBZ format")
            val tempDir = createTempDirectory(srcFile.nameWithoutExtension)

            Junrar.extract(srcFile.toFile(), tempDir.toFile())

            val destFile = Path(srcFile.parent.pathString, srcFile.nameWithoutExtension + ".cbz")
            ZipUtils.zip(destFile = destFile, content = Utils.listFiles(path = tempDir))
            tempDir.toFile().deleteRecursively()
            srcFile.toFile().delete()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun readMetadata(archiveFile: Path): Metadata? {
        val content = ZipUtils.extractFile(
            srcFile = archiveFile,
            filename = "Metadata",
            extension = "xml",
        )?.toFile()?.readText() ?: return null
        return try {
            Utils.XML_MAPPER.decodeFromString<Metadata>(content)
        } catch (mfe: MissingFieldException) {
            logger.error("${archiveFile.name} contains an invalid Metadata file: ${mfe.message}")
            null
        } catch (se: SerializationException) {
            logger.error("${archiveFile.name} contains an invalid Metadata file: ${se.message}")
            null
        } catch (nfe: NumberFormatException) {
            logger.error("${archiveFile.name} contains an invalid Metadata file: ${nfe.message}")
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun readMetronInfo(archiveFile: Path): MetronInfo? {
        val content = ZipUtils.extractFile(
            srcFile = archiveFile,
            filename = "MetronInfo",
            extension = "xml",
        )?.toFile()?.readText() ?: return null
        return try {
            Utils.XML_MAPPER.decodeFromString<MetronInfo>(content)
        } catch (mfe: MissingFieldException) {
            logger.error("${archiveFile.name} contains an invalid MetronInfo file: ${mfe.message}")
            null
        } catch (se: SerializationException) {
            logger.error("${archiveFile.name} contains an invalid MetronInfo file: ${se.message}")
            null
        } catch (nfe: NumberFormatException) {
            logger.error("${archiveFile.name} contains an invalid MetronInfo file: ${nfe.message}")
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun readComicInfo(archiveFile: Path): ComicInfo? {
        val content = ZipUtils.extractFile(
            srcFile = archiveFile,
            filename = "ComicInfo",
            extension = "xml",
        )?.toFile()?.readText() ?: return null
        return try {
            Utils.XML_MAPPER.decodeFromString<ComicInfo>(content)
        } catch (mfe: MissingFieldException) {
            logger.error("${archiveFile.name} contains an invalid ComicInfo file: ${mfe.message}")
            null
        } catch (se: SerializationException) {
            logger.error("${archiveFile.name} contains an invalid ComicInfo file: ${se.message}")
            null
        } catch (nfe: NumberFormatException) {
            logger.error("${archiveFile.name} contains an invalid ComicInfo file: ${nfe.message}")
            null
        }
    }

    fun readCollection(directory: Path): Map<Path, Metadata?> {
        val files = Utils.listFiles(directory, "cbz")
        return files.associateWith {
            readMetadata(archiveFile = it)
                ?: readMetronInfo(archiveFile = it)?.toMetadata()
                ?: readComicInfo(archiveFile = it)?.toMetadata()
        }
    }

    private fun parsePages(
        folder: Path,
        metadata: Metadata,
        filename: String,
    ) {
        val imageList = Utils.listFiles(path = folder).sorted()
        imageList.filterNot { it.extension == "xml" || it.extension == "json" }.forEachIndexed { index, it ->
            val padCount = imageList.size.toString().length
            val newPage = metadata.pages.getOrNull(index = index) == null
            val page = metadata.pages.getOrNull(index = index) ?: Metadata.Page(filename = it.name, index = index)
            val image = ImageIO.read(it.toFile())
            page.doublePage = image.width >= image.height
            page.fileSize = it.fileSize()
            page.imageHeight = image.height
            page.imageWidth = image.width
            page.index = index
            if (newPage && index == 0) {
                page.type = "Front Cover"
            }
            if (newPage && index == imageList.size - 1) {
                page.type = "Back Cover"
            }
            val newFilename = it.parent / (filename + "_${index.toString().padStart(length = padCount, padChar = '0')}." + it.extension)
            if (it.name != newFilename.name) {
                logger.info("Renamed ${it.name} to ${newFilename.name}")
                it.moveTo(newFilename, overwrite = false)
            }
            page.filename = newFilename.name

            val pages = metadata.pages.toMutableList()
            if (pages.size > index) {
                pages[index] = page
            } else {
                pages.add(page)
            }
            metadata.pages = pages.toList()
        }
    }

    private fun removeEmptyDirectories(directory: File) {
        directory.listFiles()?.forEach {
            if (it.isDirectory) {
                removeEmptyDirectories(directory = it)
            }
        }
        if (!directory.name.startsWith(".") && (directory.listFiles()?.size ?: 0) == 0) {
            logger.info("Cleaning up blank folder: ${directory.name}")
            directory.deleteRecursively()
        }
    }

    fun start(settings: Settings) {
        convertCollection(directory = settings.collectionFolder)
        var metron: MetronTalker? = null
        if (!settings.metron.username.isNullOrBlank() && !settings.metron.password.isNullOrBlank()) {
            metron = MetronTalker(settings = settings.metron)
        }
        var comicvine: ComicvineTalker? = null
        if (!settings.comicvine.apiKey.isNullOrBlank()) {
            comicvine = ComicvineTalker(settings = settings.comicvine)
        }
        readCollection(directory = settings.collectionFolder).forEach { (file, _metadata) ->
            logger.info("Processing ${file.nameWithoutExtension}")
            val metadata = _metadata ?: Metadata(
                issue = Metadata.Issue(
                    series = Metadata.Issue.Series(
                        publisher = Metadata.Issue.Series.Publisher(
                            title = Console.prompt(prompt = "Publisher title") ?: return@forEach,
                        ),
                        title = Console.prompt(prompt = "Series title") ?: return@forEach,
                    ),
                    number = Console.prompt(prompt = "Issue number") ?: return@forEach,
                ),
            )
            logger.info("Using Metron to look for information")
            var success = metron?.pullMetadata(metadata = metadata) ?: false
            if (!success) {
                logger.warn("Unable to pull info from Metron")
                logger.info("Using Comicvine to look for information")
                success = comicvine?.pullMetadata(metadata = metadata) ?: false
            }
            if (!success) {
                logger.warn("Unable to pull info from Comicvine")
            }
            val tempDir = createTempDirectory(prefix = "${file.nameWithoutExtension}_")
            ZipUtils.unzip(srcFile = file, destFolder = tempDir)
            val tempFile = file.parent / (file.name + ".temp")
            file.moveTo(target = tempFile)
            val filename = metadata.issue.getFilename()
            logger.info("Processing pages")
            parsePages(folder = tempDir, metadata = metadata, filename = filename)

            metadata.toFile(tempDir / "Metadata.xml")
            metadata.toMetronInfo()?.toFile(tempDir / "MetronInfo.xml")
            metadata.toComicInfo().toFile(tempDir / "ComicInfo.xml")

            ZipUtils.zip(destFile = file.parent / "$filename.cbz", content = Utils.listFiles(path = tempDir))
            tempDir.toFile().deleteRecursively()
            tempFile.toFile().delete()
        }
        readCollection(directory = settings.collectionFolder)
            .filterValues { it != null }
            .mapValues { it.value as Metadata }
            .forEach { (file, metadata) ->
                val newLocation = Paths.get(
                    settings.collectionFolder.pathString,
                    metadata.issue.series.publisher.getFilename(),
                    metadata.issue.series.getFilename(),
                    "${metadata.issue.getFilename()}.${file.extension}",
                )
                if (file != newLocation) {
                    logger.info(
                        "Renamed ${file.relativeTo(settings.collectionFolder)} to ${newLocation.relativeTo(settings.collectionFolder)}",
                    )
                    newLocation.parent.toFile().mkdirs()
                    file.moveTo(newLocation, overwrite = false)
                }
            }
        removeEmptyDirectories(directory = settings.collectionFolder.toFile())
    }
}

fun main(
    @Suppress("UNUSED_PARAMETER") vararg args: String,
) {
    println("Kilowog v${Utils.VERSION}")
    println("Kotlin v${KotlinVersion.CURRENT}")
    println("Java v${System.getProperty("java.version")}")
    println("Arch: ${System.getProperty("os.arch")}")
    val settings = Settings.load()
    println(settings.toString())
    App.start(settings = settings)
}
