package pl.llp.aircasting.ui.view.screens.login

import pl.llp.aircasting.data.api.responses.UserResponse
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.InternalAPIError
import pl.llp.aircasting.util.exceptions.UnexpectedAPIError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginService(
    val mSettings: Settings,
    private val mErrorHandler: ErrorHandler,
    private val mApiServiceFactory: ApiServiceFactory
) {
    fun performLogin(
        username: String, password: String,
        successCallback: () -> Unit,
        errorCallback: () -> Unit
    ) {
        val apiService = mApiServiceFactory.get(username, password)
        val call = apiService.login()

        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                when {
                    response.isSuccessful -> {
                        val body = response.body()
                        body?.let {
                            mSettings.login(body.email, body.authentication_token)
                        }
                        successCallback()
                    }
                    response.code() == 401 -> errorCallback()
                    else -> mErrorHandler.handleAndDisplay(InternalAPIError())
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                mErrorHandler.handleAndDisplay(UnexpectedAPIError(t))
            }
        })
    }
}
