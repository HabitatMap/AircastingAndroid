package pl.llp.aircasting.data.api.params

class CreateSessionBody(
    val session: String,
    val photos: List<String?>? = null,
    val compression: Boolean = true
)
