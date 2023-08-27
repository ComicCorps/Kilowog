package github.buriedincode.kilowog.services

import github.buriedincode.kilowog.Utils
import github.buriedincode.kilowog.Utils.asEnumOrNull
import github.buriedincode.kilowog.console.Console
import github.buriedincode.kilowog.models.Metadata
import github.buriedincode.kilowog.models.metadata.enums.Format
import github.buriedincode.kilowog.models.metadata.enums.Source
import github.buriedincode.kilowog.services.metron.issue.IssueEntry
import github.buriedincode.kilowog.services.metron.publisher.PublisherEntry
import github.buriedincode.kilowog.services.metron.series.SeriesEntry
import org.apache.logging.log4j.kotlin.Logging
import java.nio.file.Paths
import github.buriedincode.kilowog.Settings.Metron as MetronSettings

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

    private fun pullPublisher(metadata: Metadata): Int? {
        var publisherId = metadata.issue.publisher.resources.firstOrNull { it.source == Source.METRON }?.value
        if (publisherId == null) {
            var publisherTitle: String = metadata.issue.publisher.imprint ?: metadata.issue.publisher.title
            do {
                val publishers = this.searchPublishers(title = publisherTitle)
                val index = Console.menu(
                    choices = publishers.map { "${it.publisherId} - ${it.name}" },
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
                    publisherId = publishers[index - 1].publisherId
                }
            } while (publisherId == null)
        } else {
            Console.print("Found existing Publisher id")
            logger.info("Found existing Publisher id")
        }
        val publisher = this.metron.getPublisher(publisherId = publisherId) ?: return null
        val resources = metadata.issue.publisher.resources.toMutableSet()
        resources.add(Metadata.Issue.Resource(source = Source.METRON, value = publisherId))
        if (publisher.comicvineId != null) {
            resources.add(Metadata.Issue.Resource(source = Source.COMICVINE, value = publisher.comicvineId))
        }
        metadata.issue.publisher.resources = resources.toList()
        metadata.issue.publisher.title = publisher.name

        return publisherId
    }

    private fun searchSeries(publisherId: Int, title: String, volume: Int? = null, startYear: Int? = null): List<SeriesEntry> {
        val seriesList = this.metron.listSeries(publisherId = publisherId, title = title, volume = volume, startYear = startYear)
        if (seriesList.isEmpty()) {
            logger.warn(
                "No series found with query " +
                    "{\"publisherId\": $publisherId, \"title\": $title, \"volume\": $volume, \"startYear\": $startYear}",
            )
        }
        return seriesList
    }

    private fun pullSeries(metadata: Metadata, publisherId: Int): Int? {
        var seriesId = metadata.issue.series.resources.firstOrNull { it.source == Source.METRON }?.value
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
                )
                val index = Console.menu(
                    choices = seriesList.map { "${it.seriesId} - ${it.name}" },
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
                    seriesId = seriesList[index - 1].seriesId
                }
            } while (seriesId == null)
        } else {
            Console.print("Found existing Series id")
            logger.info("Found existing Series id")
        }
        val series = this.metron.getSeries(seriesId = seriesId) ?: return null
        val resources = metadata.issue.series.resources.toMutableSet()
        resources.add(Metadata.Issue.Resource(source = Source.METRON, value = seriesId))
        if (series.comicvineId != null) {
            resources.add(Metadata.Issue.Resource(source = Source.COMICVINE, value = series.comicvineId))
        }
        metadata.issue.series.resources = resources.toList()
        metadata.issue.series.format = series.seriesType.name.asEnumOrNull<Format>() ?: metadata.issue.series.format
        metadata.issue.series.startYear = series.yearBegan
        metadata.issue.series.title = series.name
        metadata.issue.series.volume = series.volume

        return seriesId
    }

    private fun searchIssue(seriesId: Int, number: String? = null): List<IssueEntry> {
        val issues = this.metron.listIssues(seriesId = seriesId, number = number)
        if (issues.isEmpty()) {
            logger.warn("No issues found with query {\"seriesId\": $seriesId, \"number\": $number}")
        }
        return issues
    }

    private fun pullIssue(metadata: Metadata, seriesId: Int): Int? {
        var issueId = metadata.issue.resources.firstOrNull { it.source == Source.METRON }?.value
        if (issueId == null) {
            var issueNumber: String? = metadata.issue.number
            do {
                val issues = this.searchIssue(seriesId = seriesId, number = issueNumber)
                val index = Console.menu(
                    choices = issues.map { "${it.issueId} - ${it.name}" },
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
                    issueId = issues[index - 1].issueId
                }
            } while (issueId == null)
        } else {
            Console.print("Found existing Issue id")
            logger.info("Found existing Issue id")
        }
        val issue = this.metron.getIssue(issueId = issueId) ?: return null
        val resources = metadata.issue.resources.toMutableSet()
        resources.add(Metadata.Issue.Resource(source = Source.METRON, value = issueId))
        if (issue.comicvineId != null) {
            resources.add(Metadata.Issue.Resource(source = Source.COMICVINE, value = issue.comicvineId))
        }
        metadata.issue.resources = resources.toList()
        metadata.issue.characters = issue.characters.map {
            Metadata.Issue.NamedResource(
                name = it.name,
                resources = listOf(Metadata.Issue.Resource(source = Source.METRON, value = it.id)),
            )
        }
        metadata.issue.coverDate = issue.coverDate
        metadata.issue.credits = issue.credits.map {
            Metadata.Issue.Credit(
                creator = Metadata.Issue.NamedResource(
                    name = it.creator,
                    resources = listOf(Metadata.Issue.Resource(source = Source.METRON, value = it.id)),
                ),
                roles = it.roles.map { it.name },
            )
        }
        metadata.issue.genres = issue.series.genres.map { it.name }
        metadata.issue.number = issue.number
        metadata.issue.pageCount = issue.pageCount ?: 0
        metadata.issue.storeDate = issue.storeDate
        metadata.issue.storyArcs = issue.storyArcs.map {
            Metadata.Issue.StoryArc(
                resources = listOf(Metadata.Issue.Resource(source = Source.METRON, value = it.id)),
                title = it.name,
            )
        }
        metadata.issue.summary = issue.description
        metadata.issue.teams = issue.teams.map {
            Metadata.Issue.NamedResource(
                name = it.name,
                resources = listOf(Metadata.Issue.Resource(source = Source.METRON, value = it.id)),
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
