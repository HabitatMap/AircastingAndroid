package io.lunarlogic.aircasting.screens.login

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.InternalAPIError
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.lib.AuthenticationHelper
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.responses.UserResponse
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginService(
    val mSettings: Settings,
    private val mErrorHandler: ErrorHandler,
    private val mApiServiceFactory: ApiServiceFactory,
    private val mContext: Context
) {
    private lateinit var accountManager: AccountManager

    fun performLogin(
        profile_name: String, password: String,
        successCallback: () -> Unit,
        errorCallback: () -> Unit
    ) {
        accountManager = AccountManager.get(mContext)
        val apiService = mApiServiceFactory.get(profile_name, password)
        val call = apiService.login()

        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.let {
                        addOrFindAccount(body.email, password, body.authentication_token)
                    }
                    successCallback()
                } else if (response.code() == 401) {
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

    fun addOrFindAccount(email: String, password: String, authToken: String?): Account {
        val accounts = accountManager.getAccountsByType("AirCastingAndroid")
        val account = if (accounts.isNullOrEmpty()) Account(email, "AirCastingAndroid") else accounts.first()

        if (accounts.isEmpty()) {
            finishAccountAdd(account.name, authToken, password)
            accountManager.setPassword(account, password)
            accountManager.setAuthToken(account, "oauth", authToken)
        } else {
            accountManager.setPassword(account, password)
            accountManager.setAuthToken(account, "oauth", authToken)
        }
        return account
    }

    fun finishAccountAdd(accountName: String, authToken: String?, password: String) {
        val intent = Intent()
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName)
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, "AirCastingAndroid")
        if (authToken != null) intent.putExtra(AccountManager.KEY_AUTHTOKEN, authToken)
        intent.putExtra(AccountManager.KEY_PASSWORD, password)

        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        accountManager.addAccountExplicitly(Account(accountName, "AirCastingAndroid"), password, bundle)
    }
}
