package github.comiccorps.kilowog.services.metron

import kotlinx.serialization.Serializable

@Serializable
data class ListResponse<T>(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: ArrayList<T> = ArrayList(),
)
