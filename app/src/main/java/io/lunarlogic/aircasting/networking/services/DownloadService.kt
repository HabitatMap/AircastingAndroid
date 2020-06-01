package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.responses.SessionResponse
import io.lunarlogic.aircasting.sensor.Session
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DownloadService(private val settings: Settings, private val errorHandler: ErrorHandler) {
    private val apiService = ApiServiceFactory.get(settings.getAuthToken()!!)

    fun download(uuid: String, successCallback: (Session) -> Unit) {
        val call = apiService.show(uuid)
        call.enqueue(object : Callback<SessionResponse> {
            override fun onResponse(call: Call<SessionResponse>, response: Response<SessionResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val session = Session(body!!)
                    successCallback(session)
                } else {
                    errorHandler.handle(UnexpectedAPIError())
                }
            }

            override fun onFailure(call: Call<SessionResponse>, t: Throwable) {
                errorHandler.handle(UnexpectedAPIError(t))
            }
        })
    }
}