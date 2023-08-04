package github.buriedincode.kilowog.services.comicvine

import com.fasterxml.jackson.annotation.JsonAlias

data class Response<T>(
    val error: String,
    val limit: Int,
    val offset: Int,
    @JsonAlias("number_of_page_results")
    val pageResults: Int,
    @JsonAlias("number_of_total_results")
    val totalResults: Int,
    @JsonAlias("status_code")
    val statusCode: Int,
    val results: T,
    val version: String,
)
