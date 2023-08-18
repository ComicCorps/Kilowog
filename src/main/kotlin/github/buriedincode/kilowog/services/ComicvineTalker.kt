package github.buriedincode.kilowog.services

import github.buriedincode.kilowog.console.Console
import github.buriedincode.kilowog.models.Metadata
import github.buriedincode.kilowog.models.metadata.enums.Source
import github.buriedincode.kilowog.Settings.Comicvine as ComicvineSettings

class ComicvineTalker(settings: ComicvineSettings) {
    private val comicvine: Comicvine = Comicvine(apiKey = settings.apiKey!!)

    private fun pullPublisher(metadata: Metadata): Int? {
        var publisherId = metadata.issue.publisher.resources.firstOrNull { it.source == Source.COMICVINE }?.value
        if (publisherId == null) {
            val publishers = comicvine.listPublishers(name = metadata.issue.publisher.imprint ?: metadata.issue.publisher.title)
            if (publishers.isEmpty()) {
                Console.print("No publisher found with name=${metadata.issue.publisher.imprint ?: metadata.issue.publisher.title}")
                return null
            }
            publisherId = if (publishers.size == 1) {
                Console.print("Found matching publisher: ${publishers[0].publisherId} - ${publishers[0].name}")
                publishers[0].publisherId
            } else {
                val index = Console.menu(
                    title = "Comicvine publisher select",
                    choices = publishers.map { "${it.publisherId} - ${it.name}" },
                    default = "None of the Above",
                )
                if (index == 0) {
                    return null
                }
                publishers[index - 1].publisherId
            }
        }
        val publisher = comicvine.getPublisher(publisherId = publisherId) ?: return null
        val publisherResources = metadata.issue.publisher.resources.toMutableList()
        publisherResources.add(0, Metadata.Issue.Resource(source = Source.COMICVINE, value = publisherId))
        metadata.issue.publisher.resources = publisherResources.toList()
        metadata.issue.publisher.title = publisher.name

        return publisherId
    }

    private fun pullSeries(metadata: Metadata, publisherId: Int): Int? {
        var volumeId = metadata.issue.series.resources.firstOrNull { it.source == Source.COMICVINE }?.value
        if (volumeId == null) {
            val volumes = comicvine.listVolumes(publisherId = publisherId, name = metadata.issue.series.title)
            if (volumes.isEmpty()) {
                Console.print("No volume found with name=${metadata.issue.series.title}")
                return null
            }
            volumeId = if (volumes.size == 1) {
                Console.print("Found matching volume: ${volumes[0].volumeId} - ${volumes[0].name} (${volumes[0].startYear})")
                volumes[0].volumeId
            } else {
                val index = Console.menu(
                    title = "Comicvine volume select",
                    choices = volumes.map { "${it.volumeId} - ${it.name} (${it.startYear})" },
                    default = "None of the Above",
                )
                if (index == 0) {
                    return null
                }
                volumes[index - 1].volumeId
            }
        }
        val volume = comicvine.getVolume(volumeId = volumeId) ?: return null
        val seriesResources = metadata.issue.series.resources.toMutableList()
        seriesResources.add(0, Metadata.Issue.Resource(source = Source.COMICVINE, value = volumeId))
        metadata.issue.series.resources = seriesResources.toList()
        metadata.issue.series.startYear = volume.startYear
        metadata.issue.series.title = volume.name

        return volumeId
    }

    private fun pullIssue(metadata: Metadata, seriesId: Int): Int? {
        var issueId = metadata.issue.resources.firstOrNull { it.source == Source.COMICVINE }?.value
        if (issueId == null) {
            val issues = comicvine.listIssues(volumeId = seriesId, number = metadata.issue.number)
            if (issues.isEmpty()) {
                Console.print("No issue found with number=${metadata.issue.number}")
                return null
            }
            issueId = if (issues.size == 1) {
                Console.print("Found matching issue: ${issues[0].issueId} - ${issues[0].name}")
                issues[0].issueId
            } else {
                val index = Console.menu(
                    title = "Comicvine issue select",
                    choices = issues.map { "${it.issueId} - ${it.name}" },
                    default = "None of the Above",
                )
                if (index == 0) {
                    return null
                }
                issues[index - 1].issueId
            }
        }
        val issue = comicvine.getIssue(issueId = issueId) ?: return null
        val issueResources = metadata.issue.resources.toMutableList()
        issueResources.add(0, Metadata.Issue.Resource(source = Source.COMICVINE, value = issueId))
        metadata.issue.resources = issueResources.toList()
        metadata.issue.characters = issue.characters.mapNotNull {
            Metadata.Issue.NamedResource(
                name = it.name ?: return@mapNotNull null,
                resources = listOf(Metadata.Issue.Resource(source = Source.COMICVINE, value = it.id)),
            )
        }
        metadata.issue.coverDate = issue.coverDate
        metadata.issue.credits = issue.creators.mapNotNull {
            Metadata.Issue.Credit(
                creator = Metadata.Issue.NamedResource(
                    name = it.name ?: return@mapNotNull null,
                    resources = listOf(Metadata.Issue.Resource(source = Source.COMICVINE, value = it.id)),
                ),
                roles = it.roles.split("[~\r\n]+".toRegex()).map { it.trim() },
            )
        }
        metadata.issue.locations = issue.locations.mapNotNull {
            Metadata.Issue.NamedResource(
                name = it.name ?: return@mapNotNull null,
                resources = listOf(Metadata.Issue.Resource(source = Source.COMICVINE, value = it.id)),
            )
        }
        metadata.issue.number = issue.number
        metadata.issue.storeDate = issue.storeDate
        metadata.issue.storyArcs = issue.storyArcs.mapNotNull {
            Metadata.Issue.StoryArc(
                resources = listOf(Metadata.Issue.Resource(source = Source.COMICVINE, value = it.id)),
                title = it.name ?: return@mapNotNull null,
            )
        }
        metadata.issue.summary = issue.summary
        metadata.issue.teams = issue.teams.mapNotNull {
            Metadata.Issue.NamedResource(
                name = it.name ?: return@mapNotNull null,
                resources = listOf(Metadata.Issue.Resource(source = Source.COMICVINE, value = it.id)),
            )
        }
        metadata.issue.title = issue.name

        return issueId
    }

    fun pullMetadata(metadata: Metadata): Boolean {
        val publisherId = pullPublisher(metadata = metadata) ?: return false
        val seriesId = pullSeries(metadata = metadata, publisherId = publisherId) ?: return false
        val issueId = pullIssue(metadata = metadata, seriesId = seriesId) ?: return false
        return true
    }
}
