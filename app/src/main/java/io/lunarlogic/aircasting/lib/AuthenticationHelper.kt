package io.lunarlogic.aircasting.lib

import android.R
import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.accounts.AuthenticatorException
import android.accounts.OperationCanceledException
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.IOException


class AuthenticationHelper(
    private val mContext: Context
) {

        fun getAuthToken(): String? {
            val accountManager = AccountManager.get(mContext)
            val accountType: String = "AirCastingAndroid"
            val accounts = accountManager.getAccountsByType(accountType)
            if (accounts.isEmpty()) {
                return null
            }

            /* This method retrieves the token for the given account but doesn't pop the Authentication Page if not found */
            val authToken = accountManager.peekAuthToken(accounts.first(), "oauth") //TODO: what is the authTokenType??? this moment returns null here
            //Log.i("LOGIN_SER", authToken?.toString())
            return authToken
            //return bundle.getString(AccountManager.KEY_AUTHTOKEN)
        }

    fun getEmail(): String? {
        val accountManager = AccountManager.get(mContext)
        val accountType: String = "AirCastingAndroid"
        val accounts = accountManager.getAccountsByType(accountType)

        if (accounts.isEmpty()) {
            return null
        }

        return accounts.first().name // TODO: dont know whats gonna happen when we log in with different account then we have on phone
    }

    fun removeAccount() {
        val accountManager = AccountManager.get(mContext)
        val accountType: String = "AirCastingAndroid"
        val accounts = accountManager.getAccountsByType(accountType)

        if (accounts.isEmpty()) {
            return
        }

        accountManager.removeAccount(accounts.first(), AccountManagerCallback {  }, Handler()) // TODO: dont know whats gonna happen when we log in with different account then we have on phone
    }
}
