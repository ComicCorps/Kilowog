package github.comiccorps.kilowog.services

import github.comiccorps.kilowog.Utils
import github.comiccorps.kilowog.Utils.asEnumOrNull
import github.comiccorps.kilowog.console.Console
import github.comiccorps.kilowog.models.Metadata
import github.comiccorps.kilowog.models.metadata.Credit
import github.comiccorps.kilowog.models.metadata.Format
import github.comiccorps.kilowog.models.metadata.NamedResource
import github.comiccorps.kilowog.models.metadata.Resource
import github.comiccorps.kilowog.models.metadata.Source
import github.comiccorps.kilowog.models.metadata.StoryArc
import github.comiccorps.kilowog.services.metron.issue.IssueEntry
import github.comiccorps.kilowog.services.metron.publisher.PublisherEntry
import github.comiccorps.kilowog.services.metron.series.SeriesEntry
import org.apache.logging.log4j.kotlin.Logging
import java.nio.file.Paths
import github.comiccorps.kilowog.Settings.Metron as MetronSettings

class MetronTalker(settings: MetronSettings) {
    private val metron: Metron = Metron(
        username = settings.username!!,
        password = settings.password!!,
        cache = SQLiteCache(path = Paths.get(Utils.CACHE_ROOT.toString(), "cache.sqlite"), expiry = 14),
    )

    private fun searchPublishers(title: String): List<PublisherEntry> {
        val publishers = this.metron.listPublishers(title = title)
        if (publishers.isEmpty()) {
            logger.warn("No publishers found with query {\"title\": $title}")
        }
        return publishers
    }

    private fun pullPublisher(metadata: Metadata): Long? {
        var publisherId = metadata.issue.series.publisher.resources.firstOrNull { it.source == Source.METRON }?.value
        if (publisherId == null) {
            val comicvineId = metadata.issue.series.publisher.resources.firstOrNull { it.source == Source.COMICVINE }?.value
            if (comicvineId != null) {
                val tempPublisher = this.metron.getPublisherByComicvine(comicvineId = comicvineId)
                publisherId = tempPublisher?.id
            }
        }
        if (publisherId == null) {
            var publisherTitle: String = metadata.issue.series.publisher.title
            do {
                val publishers = this.searchPublishers(title = publisherTitle).sorted()
                val index = Console.menu(
                    choices = publishers.map { "${it.id} | ${it.name}" },
                    prompt = "Select Metron Publisher",
                    default = "None of the Above",
                )
                if (index == 0) {
                    if (Console.confirm(prompt = "Try again")) {
                        publisherTitle = Console.prompt(prompt = "Publisher title") ?: return null
                    } else {
                        return null
                    }
                } else {
                    publisherId = publishers[index - 1].id
                }
            } while (publisherId == null)
        } else {
            logger.info("Found existing Publisher id")
        }
        val publisher = this.metron.getPublisher(publisherId = publisherId) ?: return null
        val resources = metadata.issue.series.publisher.resources.toMutableSet()
        resources.add(Resource(source = Source.METRON, value = publisherId))
        if (publisher.comicvineId != null) {
            resources.add(Resource(source = Source.COMICVINE, value = publisher.comicvineId))
        }
        metadata.issue.series.publisher.resources = resources.toList()
        metadata.issue.series.publisher.title = publisher.name

        return publisherId
    }

    private fun searchSeries(
        publisherId: Long,
        title: String,
        volume: Int? = null,
        startYear: Int? = null,
    ): List<SeriesEntry> {
        val seriesList = this.metron.listSeries(publisherId = publisherId, title = title, volume = volume, startYear = startYear)
        if (seriesList.isEmpty()) {
            logger.warn(
                "No series found with query " +
                    "{\"publisherId\": $publisherId, \"title\": $title, \"volume\": $volume, \"startYear\": $startYear}",
            )
        }
        return seriesList
    }

    private fun pullSeries(
        metadata: Metadata,
        publisherId: Long,
    ): Long? {
        var seriesId = metadata.issue.series.resources.firstOrNull { it.source == Source.METRON }?.value
        if (seriesId == null) {
            val comicvineId = metadata.issue.series.resources.firstOrNull { it.source == Source.COMICVINE }?.value
            if (comicvineId != null) {
                val tempSeries = this.metron.getSeriesByComicvine(comicvineId = comicvineId)
                seriesId = tempSeries?.id
            }
        }
        if (seriesId == null) {
            var seriesTitle: String = metadata.issue.series.title
            var seriesVolume: Int? = metadata.issue.series.volume
            var seriesStartYear: Int? = metadata.issue.series.startYear
            do {
                val seriesList = this.searchSeries(
                    publisherId = publisherId,
                    title = seriesTitle,
                    volume = seriesVolume,
                    startYear = seriesStartYear,
                ).sorted()
                val index = Console.menu(
                    choices = seriesList.map { "${it.id} | ${it.name}" },
                    prompt = "Select Metron Series",
                    default = "None of the Above",
                )
                if (index == 0) {
                    if (seriesStartYear != null) {
                        seriesStartYear = null
                    } else if (seriesVolume != null) {
                        seriesVolume = null
                    } else if (Console.confirm(prompt = "Try again")) {
                        seriesTitle = Console.prompt(prompt = "Series title") ?: return null
                    } else {
                        return null
                    }
                } else {
                    seriesId = seriesList[index - 1].id
                }
            } while (seriesId == null)
        } else {
            logger.info("Found existing Series id")
        }
        val series = this.metron.getSeries(seriesId = seriesId) ?: return null
        val resources = metadata.issue.series.resources.toMutableSet()
        resources.add(Resource(source = Source.METRON, value = seriesId))
        if (series.comicvineId != null) {
            resources.add(Resource(source = Source.COMICVINE, value = series.comicvineId))
        }
        metadata.issue.series.resources = resources.toList()
        if (series.seriesType.name.equals("Hard Cover", ignoreCase = true)) {
            metadata.issue.format = Format.HARDCOVER
        } else {
            metadata.issue.format = series.seriesType.name.asEnumOrNull<Format>() ?: metadata.issue.format
        }
        metadata.issue.series.startYear = series.yearBegan
        metadata.issue.series.title = series.name
        metadata.issue.series.volume = series.volume

        return seriesId
    }

    private fun searchIssue(
        seriesId: Long,
        number: String? = null,
    ): List<IssueEntry> {
        val issues = this.metron.listIssues(seriesId = seriesId, number = number)
        if (issues.isEmpty()) {
            logger.warn("No issues found with query {\"seriesId\": $seriesId, \"number\": $number}")
        }
        return issues
    }

    private fun pullIssue(
        metadata: Metadata,
        seriesId: Long,
    ): Long? {
        var issueId = metadata.issue.resources.firstOrNull { it.source == Source.METRON }?.value
        if (issueId == null) {
            val comicvineId = metadata.issue.resources.firstOrNull { it.source == Source.COMICVINE }?.value
            if (comicvineId != null) {
                val tempIssue = this.metron.getIssueByComicvine(comicvineId = comicvineId)
                issueId = tempIssue?.id
            }
        }
        if (issueId == null) {
            var issueNumber: String? = metadata.issue.number
            do {
                val issues = this.searchIssue(seriesId = seriesId, number = issueNumber).sorted()
                val index = Console.menu(
                    choices = issues.map { "${it.id} | ${it.number} - ${it.name}" },
                    prompt = "Select Metron Issue",
                    default = "None of the Above",
                )
                if (index == 0) {
                    if (Console.confirm(prompt = "Try again")) {
                        issueNumber = Console.prompt(prompt = "Issue number") ?: return null
                    } else {
                        return null
                    }
                } else {
                    issueId = issues[index - 1].id
                }
            } while (issueId == null)
        } else {
            logger.info("Found existing Issue id")
        }
        val issue = this.metron.getIssue(issueId = issueId) ?: return null
        val resources = metadata.issue.resources.toMutableSet()
        resources.add(Resource(source = Source.METRON, value = issueId))
        if (issue.comicvineId != null) {
            resources.add(Resource(source = Source.COMICVINE, value = issue.comicvineId))
        }
        metadata.issue.resources = resources.toList()
        metadata.issue.characters = issue.characters.map {
            NamedResource(
                title = it.name,
                resources = listOf(Resource(source = Source.METRON, value = it.id)),
            )
        }
        metadata.issue.coverDate = issue.coverDate
        metadata.issue.credits = issue.credits.map {
            Credit(
                creator = NamedResource(
                    title = it.creator,
                    resources = listOf(Resource(source = Source.METRON, value = it.id)),
                ),
                roles = it.roles.map {
                    NamedResource(
                        title = it.name,
                        resources = listOf(Resource(source = Source.METRON, value = it.id)),
                    )
                },
            )
        }
        metadata.issue.genres = issue.series.genres.map {
            NamedResource(title = it.name, resources = listOf(Resource(source = Source.METRON, value = it.id)))
        }
        metadata.issue.number = issue.number
        metadata.issue.pageCount = issue.pageCount ?: 0
        metadata.issue.storeDate = issue.storeDate
        metadata.issue.storyArcs = issue.storyArcs.map {
            StoryArc(
                resources = listOf(Resource(source = Source.METRON, value = it.id)),
                title = it.name,
            )
        }
        metadata.issue.summary = issue.description
        metadata.issue.teams = issue.teams.map {
            NamedResource(
                title = it.name,
                resources = listOf(Resource(source = Source.METRON, value = it.id)),
            )
        }
        metadata.issue.title = issue.title

        return issueId
    }

    fun pullMetadata(metadata: Metadata): Boolean {
        val publisherId = this.pullPublisher(metadata = metadata) ?: return false
        val seriesId = this.pullSeries(metadata = metadata, publisherId = publisherId) ?: return false
        val issueId = this.pullIssue(metadata = metadata, seriesId = seriesId) ?: return false
        return true
    }

    companion object : Logging
}
