package pl.llp.aircasting.networking.params

import okhttp3.MultipartBody
import pl.llp.aircasting.models.Photo
import retrofit2.http.Part

//todo: not sure if i can use List/Array for storing photos, according to old map vararg might be proper thing
class CreateSessionBody(@Part("session") val session: String, @Part("compression") val compression: Boolean = true, @Part("photos[]") val photos: MultipartBody.Part) // todo: according to old app, we may have varying number of "photos[]" parameters next to "session" and "compression" parameters
