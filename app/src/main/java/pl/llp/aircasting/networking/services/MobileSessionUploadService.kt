package pl.llp.aircasting.networking.services

import android.content.Context
import android.util.Log
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
import java.io.File

class MobileSessionUploadService(private val apiService: ApiService, private val errorHandler: ErrorHandler, private val context: Context) {
    fun upload(session: Session, successCallback: (response: Response<UploadSessionResponse>) -> Unit) {
        val sessionParams = SessionParams(session)

//        val sessionBody = CreateSessionBody(
//            GzippedParams.get(sessionParams, SessionParams::class.java),
//            compression = true,
//            photos = photos // there might be a few photos, we need to have variable number of arguments here
//        )
        val sessionBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("session", GzippedParams.get(sessionParams, SessionParams::class.java))
            .addFormDataPart("compression", true.toString())

        val sessionBodyWithPhotos = attachPhotos(session, sessionBody) //this one should be some sort of list
        val sessionBodyBuilt = sessionBodyWithPhotos.build()

        val call = apiService.createMobileSession(sessionBodyBuilt)
        call.enqueue(object : Callback<UploadSessionResponse> {
            override fun onResponse(call: Call<UploadSessionResponse>, response: Response<UploadSessionResponse>) {
                if (response.isSuccessful) {
                    successCallback(response)
                    Log.i("UPLOAD_CALL", response.body().toString())
                } else {
                    errorHandler.handle(UnexpectedAPIError())
                    Log.i("UPLOAD_CALL", "fail else: " + response.message())
                }
            }

            override fun onFailure(call: Call<UploadSessionResponse>, t: Throwable) {
                errorHandler.handle(UnexpectedAPIError(t))
                Log.i("UPLOAD_CALL", "fail onFailure: " + t.stackTrace)
            }
        })
    }

    fun attachPhotos(session: Session, multipartBody: MultipartBody.Builder): MultipartBody.Builder { // todo: return type must be Multipart.Body, MultipartBody as argument too
        if (session.notes.isEmpty()) {
            multipartBody.addFormDataPart("photos[]", "")
            return multipartBody
        }

        val photos = mutableListOf<Photo>()
        //var file: File = File("")
        //if (session.notes.isNotEmpty()) file = File(session.notes.first().photoPath)
        for (note in session.notes) {
            if (note.photoPath?.isNotBlank() == true && note.photoPath.isNotEmpty()) {
                //file = File(note.photoPath)
//                photos.add(BitmapTransformer.readScaledBitmap(note.photoPath, context)) // todo: it seems I dont have problem with empty "photos[]", the problem is here with correct FormData definition
                val photo = BitmapTransformer.readScaledBitmap(note.photoPath, context)
                multipartBody.addFormDataPart("photos[]", photo.filename, RequestBody.create(MediaType.parse("image/*"), photo.data))
            }
        }
//        if (photos.isNotEmpty()) Log.i("PHOTO", photos.first().toString())
//        if (photos.isEmpty()) Log.i("PHOTO", "photos: empty")
        return multipartBody
    }

//    fun attachPhotos(session: Session): MultipartBody.Part {
//        val photos = mutableListOf<Photo>()
//        var file: File = File("")
//        if (session.notes.isNotEmpty()) file = File(session.notes.first().photoPath)
//        for (note in session.notes) {
//            if (note.photoPath?.isNotBlank() == true && note.photoPath.isNotEmpty()) {
//                photos.add(BitmapTransformer.readScaledBitmap(note.photoPath, context))
//            }
//        }
//        if (photos.isNotEmpty()) Log.i("PHOTO", photos.first().toString())
//        if (photos.isEmpty()) Log.i("PHOTO", "photos: empty")
//        return MultipartBody.Part.createFormData(
//            "jpeg",
//            file.name,
//            RequestBody.create(MediaType.parse("multipart/form-data"), file)
//        )
//    }
}
