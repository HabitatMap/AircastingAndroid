package pl.llp.aircasting.networking.services

import okhttp3.MultipartBody
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.exceptions.UnexpectedAPIError
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.networking.GzippedParams
import pl.llp.aircasting.networking.params.CreateSessionBody
import pl.llp.aircasting.networking.params.SessionParams
import pl.llp.aircasting.networking.responses.UploadSessionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class FixedSessionUploadService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    fun upload(session: Session, successCallback: () -> Unit = {}) {
        session.endTime = Date()

        val sessionParams = SessionParams(session)

        val sessionBody = CreateSessionBody(
            GzippedParams.get(sessionParams, SessionParams::class.java)
        )
        val call = apiService.createFixedSession(sessionBody)
        call.enqueue(object : Callback<UploadSessionResponse> {
            override fun onResponse(call: Call<UploadSessionResponse>, response: Response<UploadSessionResponse>) {
                if (response.isSuccessful) {
                    successCallback()
                } else {
                    errorHandler.handle(UnexpectedAPIError())
                }
            }

            override fun onFailure(call: Call<UploadSessionResponse>, t: Throwable) {
                errorHandler.handle(UnexpectedAPIError(t))
            }
        })
    }

}
