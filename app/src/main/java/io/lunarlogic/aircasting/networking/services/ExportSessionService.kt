package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.networking.responses.ExportSessionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExportSessionService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {

    fun export(email: String, uuid: String) { //, successCallback: (() -> Unit)  //TODO: refactor, cleanup
//        val exportSessionParams = ExportSessionParams(email, uuid)

        val call = apiService.sendSessionByEmail(email, uuid)

        call.enqueue(object : Callback<ExportSessionResponse> {
            override fun onResponse(call: Call<ExportSessionResponse>, response: Response<ExportSessionResponse>) {
                if (response.isSuccessful) {
//                    successCallback.invoke()
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
