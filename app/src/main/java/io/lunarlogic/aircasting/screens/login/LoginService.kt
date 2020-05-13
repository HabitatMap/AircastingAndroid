package io.lunarlogic.aircasting.screens.login

import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.InternalAPIError
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.ApiServiceFactory
import io.lunarlogic.aircasting.networking.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginService(val mSettings: Settings, private val mErrorHandler: ErrorHandler) {
    fun performLogin(username: String, password: String,
                     successCallback: () -> Unit,
                     errorCallback: () -> Unit
    ) {
        val apiService = ApiServiceFactory.get(username, password)
        val call = apiService.login()

        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    response.body()?.authentication_token?.let { mSettings.setAuthToken(it) }
                    successCallback()
                } else if(response.code() == 401) {
                    errorCallback()
                } else {
                    mErrorHandler.handleAndDisplay(InternalAPIError())
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                mErrorHandler.handleAndDisplay(UnexpectedAPIError(t))
            }
        })
    }
}