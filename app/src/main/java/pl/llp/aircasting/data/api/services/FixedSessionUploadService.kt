package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.UnexpectedAPIError
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.api.GzippedParams
import pl.llp.aircasting.data.api.params.CreateSessionBody
import pl.llp.aircasting.data.api.params.SessionParams
import pl.llp.aircasting.data.api.responses.UploadSessionResponse
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