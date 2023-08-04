package github.buriedincode.kilowog.services.comicvine

import com.fasterxml.jackson.annotation.JsonAlias

data class Image(
    @JsonAlias("icon_url")
    val icon: String,
    @JsonAlias("medium_url")
    val medium: String,
    @JsonAlias("original_url")
    val original: String,
    @JsonAlias("screen_url")
    val screen: String,
    @JsonAlias("screen_large_url")
    val screenLarge: String,
    @JsonAlias("small_url")
    val small: String,
    @JsonAlias("super_url")
    val superLarge: String,
    @JsonAlias("thumb_url")
    val thumbnail: String,
    @JsonAlias("tiny_url")
    val tiny: String,
    @JsonAlias("image_tags")
    val tags: String? = null,
)
