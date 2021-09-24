package pl.llp.aircasting.networking.params

import okhttp3.MultipartBody
import pl.llp.aircasting.models.Photo
import retrofit2.http.Part

//todo: not sure if i can use List/Array for storing photos, according to old map vararg might be proper thing
class CreateSessionBody(val session: String, val compression: Boolean = true) // todo: according to old app, we may have varying number of "photos[]" parameters next to "session" and "compression" parameters
