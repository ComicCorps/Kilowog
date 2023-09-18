package github.buriedincode.kilowog.services

import com.sksamuel.hoplite.Secret
import github.buriedincode.kilowog.Utils
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

data class Metron(private val username: String, private val password: String, private val cache: SQLiteCache? = null) {
    constructor(
        username: String,
        password: Secret,
        cache: SQLiteCache? = null,
    ) : this(username = username, password = password.value, cache = cache)

    private fun encodeURI(
        endpoint: String,
        params: Map<String, String> = HashMap(),
    ): URI {
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

    private fun sendRequest(uri: URI): String? {
        if (this.cache != null) {
            val cachedResponse = cache.select(url = uri.toString())
            if (cachedResponse != null) {
                logger.debug("Using cached response for $uri")
                return cachedResponse
            }
        }
        try {
            val request = HttpRequest.newBuilder()
                .uri(uri)
                .setHeader("Accept", "application/json")
                .setHeader("User-Agent", "Kilowog-v${Utils.VERSION}/Kotlin-v${KotlinVersion.CURRENT}")
                .setHeader("Authorization", getBasicAuthenticationHeader(username, password))
                .GET()
                .build()
            val response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString())
            val level = when {
                response.statusCode() in (100 until 200) -> Level.WARN
                response.statusCode() in (200 until 300) -> Level.DEBUG
                response.statusCode() in (300 until 400) -> Level.INFO
                response.statusCode() in (400 until 500) -> Level.WARN
                else -> Level.ERROR
            }
            logger.log(level, "GET: ${response.statusCode()} - $uri")
            if (response.statusCode() == 200) {
                return response.body()
            }
            logger.error(response.body())
        } catch (exc: IOException) {
            logger.error("Unable to make request to: ${uri.path}", exc)
        } catch (exc: InterruptedException) {
            logger.error("Unable to make request to: ${uri.path}", exc)
        }
        return null
    }

    fun listPublishers(
        title: String? = null,
        page: Int = 1,
    ): List<PublisherEntry> {
        val params = HashMap<String, String>()
        params["page"] = page.toString()
        if (!title.isNullOrBlank()) {
            params["name"] = title
        }
        val uri = encodeURI(endpoint = "/publisher", params = params)
        val content = sendRequest(uri = uri)
        val response = if (content != null) Utils.JSON_MAPPER.decodeFromString<ListResponse<PublisherEntry>>(content) else null
        val results = response?.results ?: mutableListOf()
        if (results.isNotEmpty() && this.cache != null) {
            cache.insert(url = uri.toString(), response = content!!)
        }
        if (response?.next != null) {
            results.addAll(this.listPublishers(title = title, page = page + 1))
        }
        return results
    }

    fun getPublisherByComicvine(comicvineId: Long): PublisherEntry? {
        val params = mapOf("cv_id" to comicvineId.toString())
        val uri = encodeURI(endpoint = "/publisher", params = params)
        val content = sendRequest(uri = uri)
        val response = if (content != null) Utils.JSON_MAPPER.decodeFromString<ListResponse<PublisherEntry>>(content) else null
        val results = response?.results ?: mutableListOf()
        if (results.isNotEmpty() && this.cache != null) {
            cache.insert(url = uri.toString(), response = content!!)
        }
        return results.firstOrNull()
    }

    fun getPublisher(publisherId: Long): Publisher? {
        val uri = encodeURI(endpoint = "/publisher/$publisherId")
        val content = sendRequest(uri = uri)
        if (content != null && this.cache != null) {
            cache.insert(url = uri.toString(), response = content)
        }
        return if (content != null) Utils.JSON_MAPPER.decodeFromString<Publisher>(content) else null
    }

    @JvmOverloads
    fun listSeries(
        publisherId: Long,
        title: String? = null,
        volume: Int? = null,
        startYear: Int? = null,
        page: Int = 1,
    ): List<SeriesEntry> {
        val params = HashMap<String, String>()
        params["publisher_id"] = publisherId.toString()
        params["page"] = page.toString()
        if (!title.isNullOrBlank()) {
            params["name"] = title
        }
        if (volume != null) {
            params["volume"] = volume.toString()
        }
        if (startYear != null) {
            params["start_year"] = startYear.toString()
        }
        val uri = encodeURI(endpoint = "/series", params = params)
        val content = sendRequest(uri = uri)
        val response = if (content != null) Utils.JSON_MAPPER.decodeFromString<ListResponse<SeriesEntry>>(content) else null
        val results = response?.results ?: mutableListOf()
        if (results.isNotEmpty() && this.cache != null) {
            cache.insert(url = uri.toString(), response = content!!)
        }
        if (response?.next != null) {
            results.addAll(listSeries(publisherId, title, page + 1))
        }
        return results
    }

    fun getSeriesByComicvine(comicvineId: Long): SeriesEntry? {
        val params = mapOf("cv_id" to comicvineId.toString())
        val uri = encodeURI(endpoint = "/series", params = params)
        val content = sendRequest(uri = uri)
        val response = if (content != null) Utils.JSON_MAPPER.decodeFromString<ListResponse<SeriesEntry>>(content) else null
        val results = response?.results ?: mutableListOf()
        if (results.isNotEmpty() && this.cache != null) {
            cache.insert(url = uri.toString(), response = content!!)
        }
        return results.firstOrNull()
    }

    fun getSeries(seriesId: Long): Series? {
        val uri = encodeURI(endpoint = "/series/$seriesId")
        val content = sendRequest(uri = uri)
        if (content != null && this.cache != null) {
            cache.insert(url = uri.toString(), response = content)
        }
        return if (content != null) Utils.JSON_MAPPER.decodeFromString<Series>(content) else null
    }

    @JvmOverloads
    fun listIssues(
        seriesId: Long,
        number: String? = null,
        page: Int = 1,
    ): List<IssueEntry> {
        val params = HashMap<String, String>()
        params["series_id"] = seriesId.toString()
        params["page"] = page.toString()
        if (!number.isNullOrBlank()) {
            params["number"] = number
        }
        val uri = encodeURI(endpoint = "/issue", params = params)
        val content = sendRequest(uri = uri)
        val response = if (content != null) Utils.JSON_MAPPER.decodeFromString<ListResponse<IssueEntry>>(content) else null
        val results = response?.results ?: mutableListOf()
        if (results.isNotEmpty() && this.cache != null) {
            cache.insert(url = uri.toString(), response = content!!)
        }
        if (response?.next != null) {
            results.addAll(listIssues(seriesId, number, page + 1))
        }
        return results
    }

    fun getIssueByComicvine(comicvineId: Long): IssueEntry? {
        val params = mapOf("cv_id" to comicvineId.toString())
        val uri = encodeURI(endpoint = "/issue", params = params)
        val content = sendRequest(uri = uri)
        val response = if (content != null) Utils.JSON_MAPPER.decodeFromString<ListResponse<IssueEntry>>(content) else null
        val results = response?.results ?: mutableListOf()
        if (results.isNotEmpty() && this.cache != null) {
            cache.insert(url = uri.toString(), response = content!!)
        }
        return results.firstOrNull()
    }

    fun getIssue(issueId: Long): Issue? {
        val uri = encodeURI(endpoint = "/issue/$issueId")
        val content = sendRequest(uri = uri)
        if (content != null && this.cache != null) {
            cache.insert(url = uri.toString(), response = content)
        }
        return if (content != null) Utils.JSON_MAPPER.decodeFromString<Issue>(content) else null
    }

    companion object : Logging {
        private const val BASE_API = "https://metron.cloud/api"
        private val CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(5))
            .build()

        private fun getBasicAuthenticationHeader(
            username: String,
            password: String,
        ): String {
            val valueToEncode = "$username:$password"
            return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.toByteArray())
        }
    }
}
