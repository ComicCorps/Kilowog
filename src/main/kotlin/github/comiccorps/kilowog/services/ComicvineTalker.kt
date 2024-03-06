package github.comiccorps.kilowog.services

import github.comiccorps.kilowog.Utils
import github.comiccorps.kilowog.console.Console
import github.comiccorps.kilowog.models.Metadata
import github.comiccorps.kilowog.models.metadata.Credit
import github.comiccorps.kilowog.models.metadata.NamedResource
import github.comiccorps.kilowog.models.metadata.Resource
import github.comiccorps.kilowog.models.metadata.Source
import github.comiccorps.kilowog.models.metadata.StoryArc
import github.comiccorps.kilowog.services.comicvine.issue.IssueEntry
import github.comiccorps.kilowog.services.comicvine.publisher.PublisherEntry
import github.comiccorps.kilowog.services.comicvine.volume.VolumeEntry
import org.apache.logging.log4j.kotlin.Logging
import java.nio.file.Paths
import github.comiccorps.kilowog.Settings.Comicvine as ComicvineSettings

class ComicvineTalker(settings: ComicvineSettings) {
    private val comicvine: Comicvine = Comicvine(
        apiKey = settings.apiKey!!,
        cache = SQLiteCache(path = Paths.get(Utils.CACHE_ROOT.toString(), "cache.sqlite"), expiry = 14),
    )

    private fun searchPublishers(title: String): List<PublisherEntry> {
        val publishers = this.comicvine.listPublishers(title = title)
        if (publishers.isEmpty()) {
            logger.warn("No publishers found with query {\"title\": $title}")
        }
        return publishers
    }

    private fun pullPublisher(metadata: Metadata): Long? {
        var publisherId = metadata.issue.series.publisher.resources.firstOrNull { it.source == Source.COMICVINE }?.value
        if (publisherId == null) {
            var publisherTitle: String = metadata.issue.series.publisher.title
            do {
                val publishers = this.searchPublishers(title = publisherTitle).sorted()
                val index = Console.menu(
                    choices = publishers.map { "${it.id} | ${it.name}" },
                    prompt = "Select Comicvine Publisher",
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
        val publisher = this.comicvine.getPublisher(publisherId = publisherId) ?: return null
        val resources = metadata.issue.series.publisher.resources.toMutableSet()
        resources.add(Resource(source = Source.COMICVINE, value = publisherId))
        metadata.issue.series.publisher.resources = resources.toList()
        metadata.issue.series.publisher.title = publisher.name

        return publisherId
    }

    private fun searchVolumes(
        publisherId: Long,
        title: String,
        startYear: Int? = null,
    ): List<VolumeEntry> {
        val volumes = this.comicvine.listVolumes(publisherId = publisherId, title = title, startYear = startYear)
        if (volumes.isEmpty()) {
            logger.warn("No volumes found with query {\"publisherId\": $publisherId, \"title\": $title, \"startYear\": $startYear}")
        }
        return volumes
    }

    private fun pullSeries(
        metadata: Metadata,
        publisherId: Long,
    ): Long? {
        var volumeId = metadata.issue.series.resources.firstOrNull { it.source == Source.COMICVINE }?.value
        if (volumeId == null) {
            var volumeTitle: String = metadata.issue.series.title
            var volumeStartYear: Int? = metadata.issue.series.startYear
            do {
                val volumes = this.searchVolumes(publisherId = publisherId, title = volumeTitle, startYear = volumeStartYear).sorted()
                val index = Console.menu(
                    choices = volumes.map { "${it.id} | ${it.name} (${it.startYear})" },
                    prompt = "Select Comicvine Volume",
                    default = "None of the Above",
                )
                if (index == 0) {
                    if (volumeStartYear != null) {
                        volumeStartYear = null
                    } else if (Console.confirm(prompt = "Try again")) {
                        volumeTitle = Console.prompt(prompt = "Volume title") ?: return null
                    } else {
                        return null
                    }
                } else {
                    volumeId = volumes[index - 1].id
                }
            } while (volumeId == null)
        } else {
            logger.info("Found existing Volume id")
        }
        val volume = this.comicvine.getVolume(volumeId = volumeId) ?: return null
        val resources = metadata.issue.series.resources.toMutableSet()
        resources.add(Resource(source = Source.COMICVINE, value = volumeId))
        metadata.issue.series.resources = resources.toList()
        metadata.issue.series.startYear = volume.startYear
        metadata.issue.series.title = volume.name

        return volumeId
    }

    private fun searchIssues(
        volumeId: Long,
        number: String?,
    ): List<IssueEntry> {
        val issues = this.comicvine.listIssues(volumeId = volumeId, number = number)
        if (issues.isEmpty()) {
            logger.warn("No issues found with query {\"volumeId\": $volumeId, \"number\": $number}")
        }
        return issues
    }

    private fun pullIssue(
        metadata: Metadata,
        seriesId: Long,
    ): Long? {
        var issueId = metadata.issue.resources.firstOrNull { it.source == Source.COMICVINE }?.value
        if (issueId == null) {
            var issueNumber: String? = metadata.issue.number
            do {
                val issues = this.searchIssues(volumeId = seriesId, number = issueNumber).sorted()
                val index = Console.menu(
                    choices = issues.map { "${it.id} | ${it.number} - ${it.name}" },
                    prompt = "Select Comicvine Issue",
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
        val issue = this.comicvine.getIssue(issueId = issueId) ?: return null
        val resources = metadata.issue.resources.toMutableSet()
        resources.add(Resource(source = Source.COMICVINE, value = issueId))
        metadata.issue.resources = resources.toList()
        metadata.issue.characters = issue.characters.mapNotNull {
            NamedResource(
                title = it.name ?: return@mapNotNull null,
                resources = listOf(Resource(source = Source.COMICVINE, value = it.id)),
            )
        }
        metadata.issue.coverDate = issue.coverDate
        metadata.issue.credits = issue.creators.mapNotNull {
            Credit(
                creator = NamedResource(
                    title = it.name ?: return@mapNotNull null,
                    resources = listOf(Resource(source = Source.COMICVINE, value = it.id)),
                ),
                roles = it.roles.split("[~\r\n]+".toRegex()).map { NamedResource(title = it.trim()) },
            )
        }
        metadata.issue.locations = issue.locations.mapNotNull {
            NamedResource(
                title = it.name ?: return@mapNotNull null,
                resources = listOf(Resource(source = Source.COMICVINE, value = it.id)),
            )
        }
        metadata.issue.number = issue.number
        metadata.issue.storeDate = issue.storeDate
        metadata.issue.storyArcs = issue.storyArcs.mapNotNull {
            StoryArc(
                resources = listOf(Resource(source = Source.COMICVINE, value = it.id)),
                title = it.name ?: return@mapNotNull null,
            )
        }
        metadata.issue.summary = issue.summary
        metadata.issue.teams = issue.teams.mapNotNull {
            NamedResource(
                title = it.name ?: return@mapNotNull null,
                resources = listOf(Resource(source = Source.COMICVINE, value = it.id)),
            )
        }
        metadata.issue.title = issue.name

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
