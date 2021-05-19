package io.lunarlogic.aircasting.screens.login

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.lunarlogic.aircasting.authentication.Authenticator
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.InternalAPIError
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
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
) {  // TODO: maybe :AbstractAccountAuthenticator here ?? AUTHENTICATOR AS A FIELD I GUESS
    private var mAuthenticator: Authenticator? = null
    private lateinit var accountManager: AccountManager

    fun performLogin(
        profile_name: String, password: String,
        successCallback: () -> Unit,
        errorCallback: () -> Unit
    ) {
        mAuthenticator = Authenticator(mContext)
        accountManager = AccountManager.get(mContext)
        val apiService = mApiServiceFactory.get(profile_name, password) // TODO: get from account manager
        val call = apiService.login()

        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.let {
                        //mSettings.login(body.email, body.authentication_token)
                        // TODO: Add Account Manager and add account to Account Manager <??>
                        // https://www.pilanites.com/android-account-manager/   <-- building authentication activity chapter
                        addOrFindAccount(body.email, password)

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

    fun addOrFindAccount(email: String, password: String): Account {
        val accounts = accountManager.getAccountsByType("AirCastingAndroid") //AuthConstants.ACCOUNT_TYPE
        val account = if (accounts.isNullOrEmpty()) Account(email, "AirCastingAndroid") else accounts.first()

        if (accounts.isEmpty()) {
            accountManager.addAccountExplicitly(account, password, null);
        } else {
            accountManager.setPassword(accounts[0], password);
        }
        return account
    }

    fun finishAccountAdd(accountName: String, authToken: String?, password: String) {
        val intent = Intent()
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName)
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, "AirCastingAndroid")
        if (authToken != null) intent.putExtra(AccountManager.KEY_AUTHTOKEN, authToken)
        intent.putExtra(AccountManager.KEY_PASSWORD, password)
//        setAccountAuthenticatorResult(intent.extras)
        //mContextActivity.setResult(Activity.RESULT_OK, intent)
        //mContextActivity.finish() // todo ????
    }
}
