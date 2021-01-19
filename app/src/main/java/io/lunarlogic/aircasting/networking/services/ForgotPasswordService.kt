package io.lunarlogic.aircasting.networking.services

import android.content.Context
import android.widget.Toast
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.networking.params.ForgotPasswordBody
import io.lunarlogic.aircasting.networking.params.ForgotPasswordParams
import io.lunarlogic.aircasting.networking.responses.ForgotPasswordResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordService(private val mContext: Context,
                            private val mErrorHandler: ErrorHandler,
                            private val mApiServiceFactory: ApiServiceFactory) {
    fun resetPassword(login: String) {

        val apiService = mApiServiceFactory.get(emptyList())
        val forgotPasswordParams = ForgotPasswordParams(login)
        val forgotPasswordBody = ForgotPasswordBody(forgotPasswordParams)

        val call = apiService.resetPassword(forgotPasswordBody)

        call.enqueue(object: Callback<ForgotPasswordResponse> {
            override fun onResponse(
                call: Call<ForgotPasswordResponse>,
                response: Response<ForgotPasswordResponse>
            ) {
                if (response.isSuccessful){
                    Toast.makeText(mContext, mContext.getString(R.string.reset_email_sent), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.errors_network_forgot_password), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ForgotPasswordResponse>, t: Throwable) {
                mErrorHandler.handleAndDisplay(UnexpectedAPIError(t))
            }

        })
    }
}
