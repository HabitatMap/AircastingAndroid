package io.lunarlogic.aircasting.networking.services

import com.google.gson.Gson
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.networking.params.ExportSessionBody
import io.lunarlogic.aircasting.networking.params.ExportSessionParams
import io.lunarlogic.aircasting.networking.params.SessionParams
import io.lunarlogic.aircasting.networking.responses.ExportSessionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExportSessionService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    // TODO: here we would some method to do api call sending email with csv
    fun export(email: String, uuid: String, successCallback: (() -> Unit)) {
        val exportSessionParams = ExportSessionParams(email, uuid)

        val gson = Gson()
        val jsonData = gson.toJson(ExportSessionBody(exportSessionParams))
        val call = apiService.sendSessionByEmail(jsonData)

        call.enqueue(object : Callback<ExportSessionResponse> {
            override fun onResponse(call: Call<ExportSessionResponse>, response: Response<ExportSessionResponse>) {
                if (response.isSuccessful) {
                    successCallback.invoke()
                } else {
                    errorHandler.handle(UnexpectedAPIError())
//                    errorHandler.showError(context.getString(R.string.errors_edit_failure))
                }
            }

            override fun onFailure(call: Call<ExportSessionResponse>, t: Throwable) {
                errorHandler.handle(UnexpectedAPIError(t))
//                errorHandler.showError(context.getString(R.string.errors_network_required_edit))
            }
        })
    }

}
