package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.networking.responses.ExportSessionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExportSessionService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    fun export(email: String, uuid: String, successCallback: () -> Unit = {}) {
        val call = apiService.exportSession(email, uuid)

        call.enqueue(object : Callback<ExportSessionResponse> {
            override fun onResponse(call: Call<ExportSessionResponse>, response: Response<ExportSessionResponse>) {
                if (!response.isSuccessful) {
                    errorHandler.handle(UnexpectedAPIError())
                }
            }

            override fun onFailure(call: Call<ExportSessionResponse>, t: Throwable) {
                errorHandler.handle(UnexpectedAPIError(t))
            }
        })
    }
}
