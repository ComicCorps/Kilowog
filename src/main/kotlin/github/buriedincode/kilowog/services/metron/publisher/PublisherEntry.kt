package github.buriedincode.kilowog.services.metron.publisher

import com.fasterxml.jackson.annotation.JsonAlias

data class PublisherEntry(
    @JsonAlias("id")
    val publisherId: Int,
    val name: String,
    @JsonAlias("modified")
    val dateModified: String,
)
