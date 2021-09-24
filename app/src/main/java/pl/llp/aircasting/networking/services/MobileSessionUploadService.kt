package pl.llp.aircasting.networking.services

import android.content.Context
import android.net.Uri
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
    fun upload(session: Session, photoPath: String?, successCallback: (response: Response<UploadSessionResponse>) -> Unit) {
        val sessionParams = SessionParams(session)

        val session = GzippedParams.get(sessionParams, SessionParams::class.java)
        val compression = true
        val photo = if(photoPath != null) Uri.parse(photoPath!!) else Uri.parse("")

        val call = apiService.createMobileSession(session, compression, MultipartBody.Part.createFormData("file.name", photo.toString(), RequestBody.create(MediaType.parse("image/*"), photo.toString()))) // TODO: i have to pass these parameters in a different way now
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

}
