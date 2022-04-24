package pl.llp.aircasting.ui.view.screens.create_account

import com.google.gson.Gson
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.InternalAPIError
import pl.llp.aircasting.util.exceptions.UnexpectedAPIError
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.data.api.params.CreateAccountBody
import pl.llp.aircasting.data.api.params.CreateAccountParams
import pl.llp.aircasting.data.api.responses.CreateAccountErrorResponse
import pl.llp.aircasting.data.api.responses.UserResponse
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateAccountService(
    val mSettings: Settings,
    private val mErrorHandler: ErrorHandler,
    private val mApiServiceFactory: ApiServiceFactory
) {
    fun performCreateAccount(
        username: String, password: String, email: String, send_emails: Boolean,
        successCallback: () -> Unit,
        errorCallback: (CreateAccountErrorResponse) -> Unit
    ) {
        val apiService = mApiServiceFactory.get(emptyList())
        val createAccountParams = CreateAccountParams(
            username,
            password,
            email,
            send_emails
        )
        val createAccountBody = CreateAccountBody(createAccountParams)
        val call = apiService.createAccount(createAccountBody)

        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.let {
                        mSettings.login(body.email, body.authentication_token)
                    }
                    successCallback()
                } else if (response.code() == 422) {
                    val errorResponse = Gson().fromJson<CreateAccountErrorResponse>(response.errorBody()?.string(), CreateAccountErrorResponse::class.java)
                    errorCallback(errorResponse)
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
