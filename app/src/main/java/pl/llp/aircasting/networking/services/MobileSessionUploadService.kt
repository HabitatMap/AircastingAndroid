package pl.llp.aircasting.networking.services

import android.content.Context
import android.util.Log
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.exceptions.UnexpectedAPIError
import pl.llp.aircasting.lib.BitmapTransformer
import pl.llp.aircasting.models.Photo
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.networking.GzippedParams
import pl.llp.aircasting.networking.params.CreateSessionBody
import pl.llp.aircasting.networking.params.SessionParams
import pl.llp.aircasting.networking.responses.UploadSessionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MobileSessionUploadService(private val apiService: ApiService, private val errorHandler: ErrorHandler, private val context: Context) {
    fun upload(session: Session, successCallback: (response: Response<UploadSessionResponse>) -> Unit) {
        val sessionParams = SessionParams(session)
        val photos = attachPhotos(session)
        val sessionBody = CreateSessionBody(
            GzippedParams.get(sessionParams, SessionParams::class.java),
            compression = true,
            photos = photos // there might be a few photos, we need to have variable number of arguments here
        )
        val call = apiService.createMobileSession(sessionBody)
        call.enqueue(object : Callback<UploadSessionResponse> {
            override fun onResponse(call: Call<UploadSessionResponse>, response: Response<UploadSessionResponse>) {
                if (response.isSuccessful) {
                    successCallback(response)
                } else {
                    errorHandler.handle(UnexpectedAPIError())
                }
            }

            override fun onFailure(call: Call<UploadSessionResponse>, t: Throwable) {
                errorHandler.handle(UnexpectedAPIError(t))
            }
        })
    }

    fun attachPhotos(session: Session): MutableList<Photo> {
        val photos = mutableListOf<Photo>()
        for (note in session.notes) {
            if (note.photoPath?.isNotBlank() == true && note.photoPath.isNotEmpty()) {
                photos.add(BitmapTransformer.readScaledBitmap(note.photoPath, context))
            }
        }
        Log.i("PHOTO", photos.first().toString())
        return photos
    }
}
