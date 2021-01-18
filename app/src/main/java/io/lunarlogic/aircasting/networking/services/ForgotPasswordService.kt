package io.lunarlogic.aircasting.networking.services

import android.content.Context
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.networking.params.ForgotPasswordBody
import io.lunarlogic.aircasting.networking.params.ForgotPasswordParams
import io.lunarlogic.aircasting.networking.responses.ForgotPasswordResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    fun resetPassword(login: String, successCallback: (() -> Unit)) {

        val forgotPasswordParams = ForgotPasswordParams(login)
        val forgotPasswordBody = ForgotPasswordBody(forgotPasswordParams)

        val call = apiService.resetPassword(forgotPasswordBody)

        call.enqueue(object: Callback<ForgotPasswordResponse> {
            override fun onResponse(
                call: Call<ForgotPasswordResponse>,
                response: Response<ForgotPasswordResponse>
            ) {
                TODO("Not yet implemented")
            }

            override fun onFailure(call: Call<ForgotPasswordResponse>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }
}
