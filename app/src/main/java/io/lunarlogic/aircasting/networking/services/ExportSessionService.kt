package io.lunarlogic.aircasting.networking.services

import android.content.Context
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.SessionExportFailedError
import io.lunarlogic.aircasting.exceptions.SessionUploadPendingError
import io.lunarlogic.aircasting.networking.responses.ExportSessionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExportSessionService(private val apiService: ApiService, private val errorHandler: ErrorHandler, private val context: Context) {
    fun export(email: String, uuid: String, successCallback: () -> Unit = {}) {
        val call = apiService.exportSession(email, uuid)

        call.enqueue(object : Callback<ExportSessionResponse> {
            override fun onResponse(call: Call<ExportSessionResponse>, response: Response<ExportSessionResponse>) {
                if (response.isSuccessful) {
                    successCallback.invoke()
                } else {
                    errorHandler.handle(SessionUploadPendingError())
                }
            }

            override fun onFailure(call: Call<ExportSessionResponse>, t: Throwable) {
                errorHandler.handle(SessionExportFailedError(t))
            }
        })
    }
}
