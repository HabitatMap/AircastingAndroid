package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.networking.responses.UploadSessionResponse
import io.lunarlogic.aircasting.networking.GzippedSession
import io.lunarlogic.aircasting.networking.params.CreateSessionBody
import io.lunarlogic.aircasting.networking.params.SessionParams
import io.lunarlogic.aircasting.sensor.Session
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MobileSessionUploadService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    fun upload(session: Session, successCallback: () -> Unit) {
        val sessionParams = SessionParams(session)
        val sessionBody = CreateSessionBody(
            GzippedSession.get(sessionParams)
        )
        val call = apiService.createMobileSession(sessionBody)
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