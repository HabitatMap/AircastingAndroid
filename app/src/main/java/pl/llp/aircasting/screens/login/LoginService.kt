package pl.llp.aircasting.screens.login

import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.exceptions.InternalAPIError
import pl.llp.aircasting.exceptions.UnexpectedAPIError
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.networking.responses.UserResponse
import pl.llp.aircasting.networking.services.ApiServiceFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginService(
    val mSettings: Settings,
    private val mErrorHandler: ErrorHandler,
    private val mApiServiceFactory: ApiServiceFactory
) {
    fun performLogin(username: String, password: String,
                     successCallback: () -> Unit,
                     errorCallback: () -> Unit
    ) {
        val apiService = mApiServiceFactory.get(username, password)
        val call = apiService.login()

        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.let {
                        mSettings.login(body.email, body.authentication_token)
                    }
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
