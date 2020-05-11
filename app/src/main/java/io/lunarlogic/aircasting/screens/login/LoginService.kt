package io.lunarlogic.aircasting.screens.login

import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.ApiServiceFactory
import io.lunarlogic.aircasting.networking.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginService(val mSettings: Settings) {
    fun performLogin(username: String, password: String, successCallback: () -> Unit) {
        val apiService = ApiServiceFactory.get(username, password)
        val call = apiService.login()

        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                response.body()?.authentication_token?.let { mSettings.setAuthToken(it) }
                successCallback()
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                // TODO: handle
            }
        })
    }
}