package pl.llp.aircasting.data.api.params

import okhttp3.RequestBody

class CreateSessionBody(
    val session: String,
    photos: List<RequestBody?>? = null,
    val compression: Boolean = true
)
