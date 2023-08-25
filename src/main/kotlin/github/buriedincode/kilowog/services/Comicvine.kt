package github.buriedincode.kilowog.services

import com.sksamuel.hoplite.Secret
import github.buriedincode.kilowog.Utils
import github.buriedincode.kilowog.services.comicvine.Response
import github.buriedincode.kilowog.services.comicvine.issue.Issue
import github.buriedincode.kilowog.services.comicvine.issue.IssueEntry
import github.buriedincode.kilowog.services.comicvine.publisher.Publisher
import github.buriedincode.kilowog.services.comicvine.publisher.PublisherEntry
import github.buriedincode.kilowog.services.comicvine.volume.Volume
import github.buriedincode.kilowog.services.comicvine.volume.VolumeEntry
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
import java.util.stream.Collectors

data class Comicvine(private val apiKey: String, private val cache: SQLiteCache? = null) {
    constructor(apiKey: Secret, cache: SQLiteCache? = null) : this(apiKey = apiKey.value, cache = cache)
    private fun encodeURI(endpoint: String, params: MutableMap<String, String> = HashMap()): URI {
        params["api_key"] = apiKey
        params["format"] = "json"
        val encodedUrl = params.keys
            .stream()
            .sorted()
            .map { key: String -> key + "=" + URLEncoder.encode(params[key], StandardCharsets.UTF_8) }
            .collect(Collectors.joining("&", "$BASE_API$endpoint?", ""))
        return URI.create(encodedUrl)
    }

    private fun sendRequest(uri: URI): String? {
        val regex = "api_key=(.+?)&".toRegex()
        val adjustedUri = regex.replaceFirst(uri.toString(), "api_key=***&")
        if (this.cache != null) {
            val cachedResponse = cache.select(url = adjustedUri)
            if (cachedResponse != null) {
                logger.info("Using cached response for $adjustedUri")
                return cachedResponse
            }
        }
        try {
            val request = HttpRequest.newBuilder()
                .uri(uri)
                .setHeader("Accept", "application/json")
                .setHeader(
                    "User-Agent",
                    "Dex-Starr-v${Utils.VERSION}/Java-v${System.getProperty("java.version")}",
                )
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
            logger.log(level, "GET: ${response.statusCode()} - $adjustedUri")
            if (response.statusCode() == 200) {
                if (this.cache != null) {
                    cache.insert(url = adjustedUri, response = response.body())
                }
                return response.body()
            }
            logger.error(response.body())
        } catch (exc: IOException) {
            logger.error("Unable to make request to: $adjustedUri", exc)
        } catch (exc: InterruptedException) {
            logger.error("Unable to make request to: $adjustedUri", exc)
        }
        return null
    }

    @JvmOverloads
    fun listPublishers(title: String? = null, page: Int = 1): List<PublisherEntry> {
        val params = HashMap<String, String>()
        params["limit"] = PAGE_LIMIT.toString()
        params["offset"] = ((page - 1) * PAGE_LIMIT).toString()
        if (!title.isNullOrBlank()) {
            params["filter"] = "name:$title"
        }
        val content = sendRequest(uri = encodeURI(endpoint = "/publishers", params = params))
        val response = if (content != null) Utils.JSON_MAPPER.decodeFromString<Response<ArrayList<PublisherEntry>>>(content) else null
        val results = response?.results ?: mutableListOf()
        if ((response?.totalResults ?: -1) >= page * PAGE_LIMIT) {
            results.addAll(listPublishers(title, page + 1))
        }
        return results
    }

    fun getPublisher(publisherId: Int): Publisher? {
        val content = sendRequest(uri = encodeURI(endpoint = "/publisher/${Resource.PUBLISHER.resourceId}-$publisherId"))
        val response = if (content != null) Utils.JSON_MAPPER.decodeFromString<Response<Publisher>>(content) else null
        return response?.results
    }

    @JvmOverloads
    fun listVolumes(publisherId: Int, title: String? = null, startYear: Int? = null, page: Int = 1): List<VolumeEntry> {
        val params = HashMap<String, String>()
        params["limit"] = PAGE_LIMIT.toString()
        params["offset"] = ((page - 1) * PAGE_LIMIT).toString()
        if (!title.isNullOrBlank()) {
            params["filter"] = "name:$title"
        }
        val content = sendRequest(uri = encodeURI(endpoint = "/volumes", params = params))
        val response = if (content != null) Utils.JSON_MAPPER.decodeFromString<Response<ArrayList<VolumeEntry>>>(content) else null
        var results = response?.results ?: mutableListOf()
        if ((response?.totalResults ?: -1) >= page * PAGE_LIMIT) {
            results.addAll(listVolumes(publisherId, title, page + 1))
        }
        results = results.stream().filter { (it.publisher != null) && (it.publisher.id == publisherId) }.toList()
        if (startYear != null) {
            results = results.stream().filter { it.startYear == startYear }.toList()
        }
        return results
    }

    fun getVolume(volumeId: Int): Volume? {
        val content = sendRequest(uri = encodeURI(endpoint = "/volume/${Resource.VOLUME.resourceId}-$volumeId"))
        val response = if (content != null) Utils.JSON_MAPPER.decodeFromString<Response<Volume>>(content) else null
        return response?.results
    }

    @JvmOverloads
    fun listIssues(volumeId: Int, number: String? = null, page: Int = 1): List<IssueEntry> {
        val params = HashMap<String, String>()
        params["limit"] = PAGE_LIMIT.toString()
        params["offset"] = ((page - 1) * PAGE_LIMIT).toString()
        if (!number.isNullOrBlank()) {
            params["filter"] = "volume:$volumeId,issue_number:$number"
        } else {
            params["filter"] = "volume:$volumeId"
        }
        val content = sendRequest(uri = encodeURI(endpoint = "/issues", params = params))
        val response = if (content != null) Utils.JSON_MAPPER.decodeFromString<Response<ArrayList<IssueEntry>>>(content) else null
        val results = response?.results ?: mutableListOf()
        if ((response?.totalResults ?: -1) >= page * PAGE_LIMIT) {
            results.addAll(listIssues(volumeId, number, page + 1))
        }
        return results
    }

    fun getIssue(issueId: Int): Issue? {
        val content = sendRequest(uri = encodeURI(endpoint = "/issue/${Resource.ISSUE.resourceId}-$issueId"))
        val response = if (content != null) Utils.JSON_MAPPER.decodeFromString<Response<Issue>>(content) else null
        return response?.results
    }

    private enum class Resource(val resourceId: Int) {
        PUBLISHER(4010),
        VOLUME(4050),
        ISSUE(4000),
    }

    companion object : Logging {
        private const val BASE_API = "https://comicvine.gamespot.com/api"
        private val CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(5))
            .build()
        private const val PAGE_LIMIT = 100
    }
}
