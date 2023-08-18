package github.buriedincode.kilowog.services

import github.buriedincode.kilowog.console.Console
import github.buriedincode.kilowog.models.Metadata
import github.buriedincode.kilowog.models.metadata.enums.Source
import github.buriedincode.kilowog.Settings.Metron as MetronSettings

class MetronTalker(settings: MetronSettings) {
    private val metron: Metron = Metron(username = settings.username!!, password = settings.password!!)

    private fun pullPublisher(metadata: Metadata): Int? {
        var publisherId = metadata.issue.publisher.resources.firstOrNull { it.source == Source.METRON }?.value
        if (publisherId == null) {
            val publishers = metron.listPublishers(name = metadata.issue.publisher.imprint ?: metadata.issue.publisher.title)
            if (publishers.isEmpty()) {
                Console.print("No publisher found with name=${metadata.issue.publisher.imprint ?: metadata.issue.publisher.title}")
                return null
            }
            publisherId = if (publishers.size == 1) {
                Console.print("Found matching publisher: ${publishers[0].publisherId} - ${publishers[0].name}")
                publishers[0].publisherId
            } else {
                val index = Console.menu(
                    title = "Metron publisher select",
                    choices = publishers.map { "${it.publisherId} - ${it.name}" },
                    default = "None of the Above",
                )
                if (index == 0) {
                    return null
                }
                publishers[index - 1].publisherId
            }
        }
        val publisher = metron.getPublisher(publisherId = publisherId) ?: return null
        val publisherResources = metadata.issue.publisher.resources.toMutableList()
        publisherResources.add(0, Metadata.Issue.Resource(source = Source.METRON, value = publisherId))
        metadata.issue.publisher.resources = publisherResources.toList()
        metadata.issue.publisher.title = publisher.name

        return publisherId
    }

    private fun pullSeries(metadata: Metadata, publisherId: Int): Int? {
        var seriesId = metadata.issue.series.resources.firstOrNull { it.source == Source.METRON }?.value
        if (seriesId == null) {
            val seriesList = metron.listSeries(publisherId = publisherId, name = metadata.issue.series.title)
            if (seriesList.isEmpty()) {
                Console.print("No series found with name=${metadata.issue.series.title}")
                return null
            }
            seriesId = if (seriesList.size == 1) {
                Console.print("Found matching series: ${seriesList[0].seriesId} - ${seriesList[0].name}")
                seriesList[0].seriesId
            } else {
                val index = Console.menu(
                    title = "Metron series select",
                    choices = seriesList.map { "${it.seriesId} - ${it.name}" },
                    default = "None of the Above",
                )
                if (index == 0) {
                    return null
                }
                seriesList[index - 1].seriesId
            }
        }
        val series = metron.getSeries(seriesId = seriesId) ?: return null
        val seriesResources = metadata.issue.series.resources.toMutableList()
        seriesResources.add(0, Metadata.Issue.Resource(source = Source.METRON, value = seriesId))
        metadata.issue.series.resources = seriesResources.toList()
        metadata.issue.series.format = series.seriesType.name
        metadata.issue.series.startYear = series.yearBegan
        metadata.issue.series.title = series.name
        metadata.issue.series.volume = series.volume

        return seriesId
    }

    private fun pullIssue(metadata: Metadata, seriesId: Int): Int? {
        var issueId = metadata.issue.resources.firstOrNull { it.source == Source.METRON }?.value
        if (issueId == null) {
            val issues = metron.listIssues(seriesId = seriesId, number = metadata.issue.number)
            if (issues.isEmpty()) {
                Console.print("No issue found with number=${metadata.issue.number}")
                return null
            }
            issueId = if (issues.size == 1) {
                Console.print("Found matching issue: ${issues[0].issueId} - ${issues[0].name}")
                issues[0].issueId
            } else {
                val index = Console.menu(
                    title = "Metron issue select",
                    choices = issues.map { "${it.issueId} - ${it.name}" },
                    default = "None of the Above",
                )
                if (index == 0) {
                    return null
                }
                issues[index - 1].issueId
            }
        }
        val issue = metron.getIssue(issueId = issueId) ?: return null
        val issueResources = metadata.issue.resources.toMutableList()
        issueResources.add(0, Metadata.Issue.Resource(source = Source.METRON, value = issueId))
        metadata.issue.resources = issueResources.toList()
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
        metadata.issue.pageCount = issue.pageCount
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
        val publisherId = pullPublisher(metadata = metadata) ?: return false
        val seriesId = pullSeries(metadata = metadata, publisherId = publisherId) ?: return false
        val issueId = pullIssue(metadata = metadata, seriesId = seriesId) ?: return false
        return true
    }
}
