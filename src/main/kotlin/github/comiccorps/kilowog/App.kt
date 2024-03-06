package github.comiccorps.kilowog

import com.github.junrar.Junrar
import github.comiccorps.kilowog.Utils.isNullOrBlank
import github.comiccorps.kilowog.console.Console
import github.comiccorps.kilowog.models.ComicInfo
import github.comiccorps.kilowog.models.Metadata
import github.comiccorps.kilowog.models.MetronInfo
import github.comiccorps.kilowog.models.metadata.Issue
import github.comiccorps.kilowog.models.metadata.Meta
import github.comiccorps.kilowog.models.metadata.TitledResource
import github.comiccorps.kilowog.models.metadata.Page
import github.comiccorps.kilowog.models.metadata.PageType
import github.comiccorps.kilowog.models.metadata.Series
import github.comiccorps.kilowog.models.metadata.Tool
import github.comiccorps.kilowog.services.ComicvineTalker
import github.comiccorps.kilowog.services.MetronTalker
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.kotlin.Logging
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import javax.imageio.ImageIO
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
    var comicvine: ComicvineTalker? = null
    var metron: MetronTalker? = null

    private fun convertCollection(directory: Path) {
        Utils.listFiles(directory, "cbr").forEach { srcFile ->
            logger.info("Converting ${srcFile.name} to CBZ format")
            val tempDir = createTempDirectory(srcFile.nameWithoutExtension)

            Junrar.extract(srcFile.toFile(), tempDir.toFile())

            val destinationFile = srcFile.parent / "${srcFile.nameWithoutExtension}.cbz"
            ZipUtils.zip(archiveFile = destinationFile, content = Utils.listFiles(path = tempDir))
            tempDir.toFile().deleteRecursively()
            srcFile.toFile().delete()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun readMetadata(archiveFile: Path): Metadata? {
        val content = ZipUtils.extractFile(
            archiveFile = archiveFile,
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
    private fun readMetronInfo(archiveFile: Path): MetronInfo? {
        val content = ZipUtils.extractFile(
            archiveFile = archiveFile,
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
    private fun readComicInfo(archiveFile: Path): ComicInfo? {
        val content = ZipUtils.extractFile(
            archiveFile = archiveFile,
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

    private fun readCollection(directory: Path): Map<Path, Metadata?> {
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
        val imageList = Utils.listFiles(path = folder, extensions = Utils.imageExtensions).sorted()
        val padLength = imageList.size.toString().length
        imageList.forEachIndexed { index, it ->
            val page = if (metadata.pages.getOrNull(index = index) == null) {
                val image = ImageIO.read(it.toFile())
                Page(
                    doublePage = image.width >= image.height,
                    filename = it.name,
                    size = it.fileSize(),
                    height = image.height,
                    width = image.width,
                    index = index,
                    type = when (index) {
                        0 -> PageType.FRONT_COVER
                        imageList.size - 1 -> PageType.BACK_COVER
                        else -> PageType.STORY
                    },
                )
            } else {
                metadata.pages.get(index = index)
            }

            val newFilename = it.parent / (filename + "_${index.toString().padStart(length = padLength, padChar = '0')}." + it.extension)
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
            logger.info("Removing blank folder: ${directory.name}")
            directory.deleteRecursively()
        }
    }

    private fun extractToTemporary(file: Path): Path {
        val tempDir = createTempDirectory(prefix = "${file.nameWithoutExtension}_")
        ZipUtils.unzip(archiveFile = file, destinationFolder = tempDir)
        val tempFile = file.parent / (file.name + ".temp")
        file.moveTo(target = tempFile)
        return tempDir
    }

    fun start(
        settings: Settings,
        force: Boolean = false,
    ) {
        convertCollection(directory = settings.collectionFolder)
        if (settings.metron != null && !settings.metron.username.isNullOrBlank() && !settings.metron.password.isNullOrBlank()) {
            metron = MetronTalker(settings = settings.metron)
        }
        if (settings.comicvine != null && !settings.comicvine.apiKey.isNullOrBlank()) {
            comicvine = ComicvineTalker(settings = settings.comicvine)
        }

        readCollection(directory = settings.collectionFolder).forEach { (file, _metadata) ->
            val (metadata, _folder) = if (_metadata == null) {
                logger.info("Processing ${file.nameWithoutExtension}")
                val tempFolder = extractToTemporary(file = file)
                Metadata(
                    issue = Issue(
                        series = Series(
                            publisher = TitledResource(
                                title = Console.prompt(prompt = "Publisher title") ?: return@forEach,
                            ),
                            title = Console.prompt(prompt = "Series title") ?: return@forEach,
                        ),
                        number = Console.prompt(prompt = "Issue number") ?: return@forEach,
                        pageCount = Utils.listFiles(path = tempFolder, extensions = Utils.imageExtensions).size,
                    ),
                    meta = Meta(date = LocalDate.now().toKotlinLocalDate(), tool = Tool(value = "Manual")),
                ) to tempFolder
            } else {
                _metadata to null
            }

            if (!force) {
                val now = LocalDate.now()
                if (metadata.meta.tool == Tool() && metadata.meta.date.toJavaLocalDate().isAfter(now.minusDays(28))) {
                    return@forEach
                }
            }
            if (_metadata != null)
                logger.info("Processing ${file.nameWithoutExtension}")

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

            val filename = metadata.issue.getFilename()
            logger.info("Processing pages")
            val tempFolder = _folder ?: extractToTemporary(file = file)
            parsePages(folder = tempFolder, metadata = metadata, filename = filename)
            metadata.meta = Meta(date = LocalDate.now().toKotlinLocalDate())
            metadata.schemaUrl = Metadata.schemaUrl

            if (settings.output.createMetadata) {
                metadata.toFile(tempFolder / "Metadata.xml")
            }
            if (settings.output.createMetronInfo) {
                metadata.toMetronInfo()?.toFile(tempFolder / "MetronInfo.xml")
            }
            if (settings.output.createComicInfo) {
                metadata.toComicInfo().toFile(tempFolder / "ComicInfo.xml")
            }

            ZipUtils.zip(
                archiveFile = file.parent / "$filename.cbz",
                content = Utils.listFiles(path = tempFolder, "xml", *Utils.imageExtensions),
            )
            tempFolder.toFile().deleteRecursively()
            (file.parent / (file.name + ".temp")).toFile().delete()
        }
        readCollection(directory = settings.collectionFolder)
            .filterValues { it != null }
            .mapValues { it.value as Metadata }
            .forEach { (file, metadata) ->
                val newLocation = Paths.get(
                    settings.collectionFolder.pathString,
                    Utils.sanitize(value = metadata.issue.series.publisher.title),
                    metadata.issue.series.getFilename(),
                    "${metadata.issue.getFilename()}.${file.extension}",
                )
                if (file != newLocation) {
                    logger.info(
                        "Moved ${file.relativeTo(settings.collectionFolder)} to ${newLocation.relativeTo(settings.collectionFolder)}",
                    )
                    newLocation.parent.toFile().mkdirs()
                    file.moveTo(newLocation, overwrite = false)
                }
            }
        removeEmptyDirectories(directory = settings.collectionFolder.toFile())
    }
}

fun main(vararg args: String) {
    println("Kilowog v${Utils.VERSION}")
    println("Kotlin v${KotlinVersion.CURRENT}")
    println("Java v${System.getProperty("java.version")}")
    println("Arch: ${System.getProperty("os.arch")}")

    println("Args: ${args.contentToString()}")

    val settings = Settings.load()
    println(settings.toString())

    App.start(settings = settings, force = args.firstOrNull().equals("force", ignoreCase = true))
}
