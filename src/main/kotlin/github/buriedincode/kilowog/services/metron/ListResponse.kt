package github.buriedincode.kilowog.services.metron

data class ListResponse<T>(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: ArrayList<T> = ArrayList(),
)
