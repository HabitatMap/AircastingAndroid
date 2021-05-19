package io.lunarlogic.aircasting.screens.create_account

import android.accounts.AccountManager
import android.content.Context
import com.google.gson.Gson
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.InternalAPIError
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.params.CreateAccountBody
import io.lunarlogic.aircasting.networking.params.CreateAccountParams
import io.lunarlogic.aircasting.networking.responses.CreateAccountErrorResponse
import io.lunarlogic.aircasting.networking.responses.UserResponse
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateAccountService(
    val mSettings: Settings,
    private val mErrorHandler: ErrorHandler,
    private val mApiServiceFactory: ApiServiceFactory,
    private val mContext: Context
) {
    fun performCreateAccount(
        profile_name: String, password: String, email: String, send_emails: Boolean,
        successCallback: () -> Unit,
        errorCallback: (CreateAccountErrorResponse) -> Unit
    ) {
        val apiService = mApiServiceFactory.get(emptyList())
        val createAccountParams = CreateAccountParams(
            profile_name,
            password,
            email,
            send_emails
        )
        val createAccountBody = CreateAccountBody(createAccountParams)
        val accountManager = AccountManager.get(mContext)
        val call = apiService.createAccount(createAccountBody)

        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.let {
                        //mSettings.login(body.email, body.authentication_token)
                        // todo: accountManager.login ??
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
