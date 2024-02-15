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
import kotlinx.serialization.SerializationException
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
        var encodedUrl = "$BASE_API$endpoint/"
        if (params.isNotEmpty()) {
            encodedUrl = params.keys
                .stream()
                .sorted()
                .map {
                    "$it=${URLEncoder.encode(params[it], StandardCharsets.UTF_8)}"
                }
                .collect(Collectors.joining("&", "$encodedUrl?", ""))
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
        } catch (ioe: IOException) {
            logger.error("Unable to make request to: ${uri.path}", ioe)
        } catch (ie: InterruptedException) {
            logger.error("Unable to make request to: ${uri.path}", ie)
        }
        return null
    }

    private fun listPublishers(params: Map<String, String> = emptyMap()): List<PublisherEntry> {
        val uri = encodeURI(endpoint = "/publisher", params = params)
        try {
            val content: String = sendRequest(uri = uri) ?: return emptyList()
            val response: ListResponse<PublisherEntry> = Utils.JSON_MAPPER.decodeFromString(content)
            val results = response.results
            if (results.isNotEmpty() && this.cache != null) {
                cache.insert(url = uri.toString(), response = content)
            }
            if (response.next != null) {
                val temp = params.toMutableMap()
                if ("page" in temp) {
                    temp["page"] = (temp["page"]!!.toInt() + 1).toString()
                } else {
                    temp["page"] = 2.toString()
                }
                results.addAll(this.listPublishers(params = temp))
            }
            return results
        } catch (se: SerializationException) {
            logger.error("Unable to parse response", se)
            return emptyList()
        }
    }

    @JvmOverloads
    fun listPublishers(title: String? = null): List<PublisherEntry> {
        val params: Map<String, String> = if (title.isNullOrBlank()) emptyMap() else mapOf("name" to title)
        return listPublishers(params = params)
    }

    fun getPublisherByComicvine(comicvineId: Long): PublisherEntry? {
        return listPublishers(params = mutableMapOf("cv_id" to comicvineId.toString())).firstOrNull()
    }

    fun getPublisher(publisherId: Long): Publisher? {
        val uri = encodeURI(endpoint = "/publisher/$publisherId")
        try {
            val content: String = sendRequest(uri = uri) ?: return null
            if (this.cache != null) {
                cache.insert(url = uri.toString(), response = content)
            }
            return Utils.JSON_MAPPER.decodeFromString(content)
        } catch (se: SerializationException) {
            logger.error("Unable to parse response", se)
            return null
        }
    }

    private fun listSeries(params: Map<String, String> = emptyMap()): List<SeriesEntry> {
        val uri = encodeURI(endpoint = "/series", params = params)
        try {
            val content: String = sendRequest(uri = uri) ?: return emptyList()
            val response: ListResponse<SeriesEntry> = Utils.JSON_MAPPER.decodeFromString(content)
            val results = response.results
            if (results.isNotEmpty() && this.cache != null) {
                cache.insert(url = uri.toString(), response = content)
            }
            if (response.next != null) {
                val temp = params.toMutableMap()
                if ("page" in temp) {
                    temp["page"] = (temp["page"]!!.toInt() + 1).toString()
                } else {
                    temp["page"] = 2.toString()
                }
                results.addAll(this.listSeries(params = temp))
            }
            return results
        } catch (se: SerializationException) {
            logger.error("Unable to parse response", se)
            return emptyList()
        }
    }

    @JvmOverloads
    fun listSeries(
        publisherId: Long,
        title: String? = null,
        volume: Int? = null,
        startYear: Int? = null,
    ): List<SeriesEntry> {
        val params: MutableMap<String, String> = mutableMapOf(
            "publisher_id" to publisherId.toString(),
        )
        if (!title.isNullOrBlank()) {
            params["name"] = title
        }
        if (volume != null) {
            params["volume"] = volume.toString()
        }
        if (startYear != null) {
            params["start_year"] = startYear.toString()
        }
        return listSeries(params = params)
    }

    fun getSeriesByComicvine(comicvineId: Long): SeriesEntry? {
        return listSeries(params = mutableMapOf("cv_id" to comicvineId.toString())).firstOrNull()
    }

    fun getSeries(seriesId: Long): Series? {
        val uri = encodeURI(endpoint = "/series/$seriesId")
        try {
            val content: String = sendRequest(uri = uri) ?: return null
            val response: Series = Utils.JSON_MAPPER.decodeFromString(content)
            if (this.cache != null) {
                cache.insert(url = uri.toString(), response = content)
            }
            return response
        } catch (se: SerializationException) {
            logger.error("Unable to parse response", se)
            return null
        }
    }

    private fun listIssues(params: Map<String, String> = emptyMap()): List<IssueEntry> {
        val uri = encodeURI(endpoint = "/issue", params = params)
        try {
            val content: String = sendRequest(uri = uri) ?: return emptyList()
            val response: ListResponse<IssueEntry> = Utils.JSON_MAPPER.decodeFromString(content)
            val results = response.results
            if (results.isNotEmpty() && this.cache != null) {
                cache.insert(url = uri.toString(), response = content)
            }
            if (response.next != null) {
                val temp = params.toMutableMap()
                if ("page" in temp) {
                    temp["page"] = (temp["page"]!!.toInt() + 1).toString()
                } else {
                    temp["page"] = 2.toString()
                }
                results.addAll(this.listIssues(params = temp))
            }
            return results
        } catch (se: SerializationException) {
            logger.error("Unable to parse response", se)
            return emptyList()
        }
    }

    @JvmOverloads
    fun listIssues(
        seriesId: Long,
        number: String? = null,
    ): List<IssueEntry> {
        val params: MutableMap<String, String> = mutableMapOf(
            "series_id" to seriesId.toString(),
        )
        if (!number.isNullOrBlank()) {
            params["number"] = number
        }
        return listIssues(params = params)
    }

    fun getIssueByComicvine(comicvineId: Long): IssueEntry? {
        return listIssues(params = mutableMapOf("cv_id" to comicvineId.toString())).firstOrNull()
    }

    fun getIssue(issueId: Long): Issue? {
        val uri = encodeURI(endpoint = "/issue/$issueId")
        try {
            val content: String = sendRequest(uri = uri) ?: return null
            val response: Issue = Utils.JSON_MAPPER.decodeFromString(content)
            if (this.cache != null) {
                cache.insert(url = uri.toString(), response = content)
            }
            return response
        } catch (se: SerializationException) {
            logger.error("Unable to parse response", se)
            return null
        }
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
