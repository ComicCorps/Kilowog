package github.buriedincode.kilowog.services

import com.fasterxml.jackson.core.type.TypeReference
import github.buriedincode.kilowog.Utils.JSON_MAPPER
import github.buriedincode.kilowog.Utils.VERSION
import github.buriedincode.kilowog.services.metron.ListResponse
import github.buriedincode.kilowog.services.metron.issue.Issue
import github.buriedincode.kilowog.services.metron.issue.IssueEntry
import github.buriedincode.kilowog.services.metron.publisher.Publisher
import github.buriedincode.kilowog.services.metron.publisher.PublisherEntry
import github.buriedincode.kilowog.services.metron.series.Series
import github.buriedincode.kilowog.services.metron.series.SeriesEntry
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.kotlin.Logging
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.Base64
import java.util.stream.Collectors

data class Metron(private val username: String, private val password: String) {
    private fun encodeURI(endpoint: String, params: Map<String, String> = HashMap()): URI {
        var encodedUrl: String = BASE_API + endpoint
        if (params.isNotEmpty()) {
            encodedUrl = params.keys
                .stream()
                .sorted()
                .map {
                    "$it=${URLEncoder.encode(params[it], StandardCharsets.UTF_8)}"
                }
                .collect(Collectors.joining("&", "$BASE_API$endpoint?", ""))
        }
        return URI.create(encodedUrl)
    }

    private fun <T> sendRequest(uri: URI, type: TypeReference<T>): T? {
        try {
            val request = HttpRequest.newBuilder()
                .uri(uri)
                .setHeader("Accept", "application/json")
                .setHeader("User-Agent", "Kilowog-v$VERSION/Kotlin-v${KotlinVersion.CURRENT}")
                .setHeader("Authorization", getBasicAuthenticationHeader(username, password))
                .GET()
                .build()
            val response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString())
            val level = when {
                response.statusCode() in (100 until 200) -> Level.WARN
                response.statusCode() in (200 until 300) -> Level.INFO
                response.statusCode() in (300 until 400) -> Level.INFO
                response.statusCode() in (400 until 500) -> Level.WARN
                else -> Level.ERROR
            }
            logger.log(level, "GET: ${response.statusCode()} - $uri")
            if (response.statusCode() == 200) {
                return JSON_MAPPER.readValue(response.body(), type)
            }
            logger.error(response.body())
        } catch (exc: IOException) {
            logger.error("Unable to make request to: ${uri.path}", exc)
        } catch (exc: InterruptedException) {
            logger.error("Unable to make request to: ${uri.path}", exc)
        }
        return null
    }

    fun listPublishers(name: String? = null, page: Int = 1): List<PublisherEntry> {
        val params = HashMap<String, String>()
        params["page"] = page.toString()
        if (!name.isNullOrBlank()) {
            params["name"] = name
        }
        val response = sendRequest(
            uri = encodeURI(endpoint = "/publisher", params = params),
            type = object : TypeReference<ListResponse<PublisherEntry>>() {},
        )
        val results = response?.results ?: mutableListOf()
        if (response?.next != null) {
            results.addAll(this.listPublishers(name, page + 1))
        }
        return results
    }

    fun getPublisher(publisherId: Int): Publisher? {
        return sendRequest(
            uri = encodeURI(endpoint = "/publisher/$publisherId"),
            type = object : TypeReference<Publisher>() {},
        )
    }

    @JvmOverloads
    fun listSeries(publisherId: Int, name: String? = null, page: Int = 1): List<SeriesEntry> {
        val params = HashMap<String, String>()
        params["publisher_id"] = publisherId.toString()
        params["page"] = page.toString()
        if (!name.isNullOrBlank()) {
            params["name"] = name
        }
        val response = sendRequest(
            uri = encodeURI(endpoint = "/series", params = params),
            type = object : TypeReference<ListResponse<SeriesEntry>>() {},
        )
        val results = response?.results ?: mutableListOf()
        if (response?.next != null) {
            results.addAll(listSeries(publisherId, name, page + 1))
        }
        return results
    }

    fun getSeries(seriesId: Int): Series? {
        return sendRequest(
            uri = encodeURI(endpoint = "/series/$seriesId"),
            type = object : TypeReference<Series>() {},
        )
    }

    @JvmOverloads
    fun listIssues(seriesId: Int, number: String? = null, page: Int = 1): List<IssueEntry> {
        val params = HashMap<String, String>()
        params["series_id"] = seriesId.toString()
        params["page"] = page.toString()
        if (!number.isNullOrBlank()) {
            params["number"] = number
        }
        val response = sendRequest(
            uri = encodeURI(endpoint = "/issue", params = params),
            type = object : TypeReference<ListResponse<IssueEntry>>() {},
        )
        val results = response?.results ?: mutableListOf()
        if (response?.next != null) {
            results.addAll(listIssues(seriesId, number, page + 1))
        }
        return results
    }

    fun getIssue(issueId: Int): Issue? {
        return sendRequest(
            uri = encodeURI(endpoint = "/issue/$issueId"),
            type = object : TypeReference<Issue>() {},
        )
    }

    companion object : Logging {
        private const val BASE_API = "https://metron.cloud/api"
        private val CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(5))
            .build()

        private fun getBasicAuthenticationHeader(username: String, password: String): String {
            val valueToEncode = "$username:$password"
            return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.toByteArray())
        }
    }
}
