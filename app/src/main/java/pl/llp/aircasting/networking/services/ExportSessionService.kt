package pl.llp.aircasting.networking.services

import android.content.Context
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.exceptions.SessionExportFailedError
import pl.llp.aircasting.exceptions.SessionUploadPendingError
import pl.llp.aircasting.networking.responses.ExportSessionResponse
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
                    errorHandler.handleAndDisplay(SessionUploadPendingError())
                }
            }

            override fun onFailure(call: Call<ExportSessionResponse>, t: Throwable) {
                errorHandler.handleAndDisplay(SessionExportFailedError(t))
            }
        })
    }
}
