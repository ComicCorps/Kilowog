package github.buriedincode.kilowog

import com.github.junrar.Archive
import com.github.junrar.rarfile.FileHeader
import github.buriedincode.kilowog.comicinfo.ComicInfo
import github.buriedincode.kilowog.metadata.Metadata
import github.buriedincode.kilowog.metadata.enums.Source
import github.buriedincode.kilowog.metroninfo.MetronInfo
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.kotlin.Logging
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipFile
import kotlin.io.path.createTempFile
import kotlin.io.path.extension

object App : Logging {
    private fun testReadAndWrite() {
        val startMetadata = Metadata(
            issue = Metadata.Issue(
                publisher = Metadata.Issue.Publisher(
                    resources = listOf(
                        Metadata.Issue.Resource(
                            source = Source.COMICVINE,
                            value = 1868,
                        ),
                        Metadata.Issue.Resource(
                            source = Source.LEAGUE_OF_COMIC_GEEKS,
                            value = 13,
                        ),
                        Metadata.Issue.Resource(
                            source = Source.METRON,
                            value = 20,
                        ),
                    ),
                    title = "BOOM! Studios",
                ),
                series = Metadata.Issue.Series(
                    format = "Comic",
                    resources = listOf(
                        Metadata.Issue.Resource(
                            source = Source.COMICVINE,
                            value = 135280,
                        ),
                        Metadata.Issue.Resource(
                            source = Source.LEAGUE_OF_COMIC_GEEKS,
                            value = 150717,
                        ),
                    ),
                    startYear = 2021,
                    title = "Magic: The Gathering",
                    volume = 1,
                ),
                number = "1",
                title = "Magic: The Gathering #1",
                characters = listOf(
                    Metadata.Issue.NamedResource(name = "Jace Beleren"),
                    Metadata.Issue.NamedResource(name = "Kaya"),
                    Metadata.Issue.NamedResource(name = "Lavinia"),
                    Metadata.Issue.NamedResource(name = "Ral Zarek"),
                    Metadata.Issue.NamedResource(name = "Vraska"),
                ),
                coverDate = LocalDate(year = 2021, monthNumber = 4, dayOfMonth = 7),
                credits = listOf(
                    Metadata.Issue.Credit(
                        creator = Metadata.Issue.NamedResource(name = "Aaron Bartling"),
                        roles = listOf("Variant Cover Artist"),
                    ),
                    Metadata.Issue.Credit(
                        creator = Metadata.Issue.NamedResource(name = "Ig Guara"),
                        roles = listOf("Artist", "Variant Cover Artist"),
                    ),
                    Metadata.Issue.Credit(
                        creator = Metadata.Issue.NamedResource(name = "Scott Newman"),
                        roles = listOf("Designer"),
                    ),
                ),
                genres = listOf("Fantasy", "Other"),
                language = "en",
                locations = listOf(
                    Metadata.Issue.NamedResource(name = "Ravnica"),
                    Metadata.Issue.NamedResource(name = "Zendikar"),
                ),
                pageCount = 32,
                resources = listOf(
                    Metadata.Issue.Resource(
                        source = Source.COMICVINE,
                        value = 842154,
                    ),
                    Metadata.Issue.Resource(
                        source = Source.LEAGUE_OF_COMIC_GEEKS,
                        value = 3694173,
                    ),
                ),
                storeDate = LocalDate(year = 2021, monthNumber = 4, dayOfMonth = 7),
                summary = "* A new beginning for the pop culture",
                teams = listOf(
                    Metadata.Issue.NamedResource(name = "Planeswalkers"),
                ),
            ),
            notes = "Scraped metadata from Comixology [CMXDB929703], [ASINB08ZKFQBRJ]",
        )
        logger.warn(startMetadata)
        val startMetadataStr = Utils.XML_MAPPER.encodeToString(startMetadata)
        logger.warn(startMetadataStr)
        val replayMetadata = Utils.XML_MAPPER.decodeFromString<Metadata>(startMetadataStr)
        logger.warn(replayMetadata)
    }

    private fun findInfoFile(archive: Archive, filename: String): FileHeader? {
        for (fileHeader in archive.fileHeaders) {
            if (fileHeader.fileName.equals(filename, ignoreCase = true)) {
                return fileHeader
            }
        }
        return null
    }

    private fun readInfoFile(archiveFile: File, infoFile: String): String? {
        val tempFile = createTempFile(prefix = "${archiveFile.name}__${infoFile}__", suffix = ".xml").toFile()
        tempFile.deleteOnExit()

        if (archiveFile.extension == "cbz") {
            val zip = ZipFile(archiveFile)
            val entry = zip.getEntry("$infoFile.xml") ?: return null
            zip.getInputStream(entry).use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            zip.close()
        } else if (archiveFile.extension == ".cbr") {
            val archive = Archive(archiveFile)
            val fileHeader = findInfoFile(archive = archive, filename = "$infoFile.xml") ?: return null
            val fos = FileOutputStream(tempFile)
            archive.extractFile(fileHeader, fos)
            fos.close()
        } else {
            return null
        }
        return tempFile.readText()
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun readMetadata(archiveFile: File): Metadata? {
        val content = readInfoFile(archiveFile = archiveFile, infoFile = "Metadata")
            ?: return null
        return try {
            Utils.XML_MAPPER.decodeFromString<Metadata>(content)
        } catch (exc: MissingFieldException) {
            // logger.fatal(content)
            logger.fatal(exc)
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun readMetronInfo(archiveFile: File): MetronInfo? {
        val content = readInfoFile(archiveFile = archiveFile, infoFile = "MetronInfo")
            ?: return null
        return try {
            Utils.XML_MAPPER.decodeFromString<MetronInfo>(content)
        } catch (exc: MissingFieldException) {
            // logger.fatal(content)
            logger.fatal(exc)
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun readComicInfo(archiveFile: File): ComicInfo? {
        val content = readInfoFile(archiveFile = archiveFile, infoFile = "ComicInfo")
            ?: return null
        return try {
            Utils.XML_MAPPER.decodeFromString<ComicInfo>(content)
        } catch (exc: MissingFieldException) {
            // logger.fatal(content)
            logger.fatal(exc)
            null
        }
    }

    fun readCollection(settings: Settings): Map<Path, Metadata?> {
        val files = Utils.listFiles(settings.collectionFolder, "cbz", "cbr")
        return files.associateWith {
            readMetadata(archiveFile = it.toFile())
                ?: readMetronInfo(archiveFile = it.toFile())?.toMetadata()
                ?: readComicInfo(archiveFile = it.toFile())?.toMetadata()
        }
    }

    fun start(settings: Settings) {
        // testReadAndWrite()
        val collection = readCollection(settings = settings)
        println(collection.keys)
        collection.filterValues { it != null }.mapValues { it.value as Metadata }.forEach { (file, metadata) ->
            val newLocation = Paths.get(
                settings.collectionFolder.toString(),
                metadata.issue.publisher.getFilename(),
                metadata.issue.series.getFilename(),
                "${metadata.issue.getFilename()}.${file.extension}",
            )
            println("$file => $newLocation")
        }
    }
}

fun main(vararg args: String) {
    println("Kilowog v${Utils.VERSION}")
    println("Kotlin v${KotlinVersion.CURRENT}")
    println("Java v${System.getProperty("java.version")}")
    println("Arch: ${System.getProperty("os.arch")}")
    val settings = Settings.load()
    println(settings.toString())
    App.start(settings = settings)
}
