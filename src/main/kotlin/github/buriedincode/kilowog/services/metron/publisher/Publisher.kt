package github.buriedincode.kilowog.services.metron.publisher

import com.fasterxml.jackson.annotation.JsonAlias

data class Publisher(
    @JsonAlias("id")
    val publisherId: Int,
    val name: String,
    val founded: Int,
    @JsonAlias("desc")
    val description: String? = null,
    @JsonAlias("image")
    val imageUrl: String,
    @JsonAlias("resource_url")
    val resourceUrl: String,
    @JsonAlias("modified")
    val dateModified: String,
)
