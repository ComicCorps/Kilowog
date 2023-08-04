package github.buriedincode.kilowog.services.comicvine

import com.fasterxml.jackson.annotation.JsonAlias

data class AlternativeImage(
    val caption: String? = null,
    val id: Int,
    @JsonAlias("image_tags")
    val tags: String,
    @JsonAlias("original_url")
    val originalUrl: String,
)
