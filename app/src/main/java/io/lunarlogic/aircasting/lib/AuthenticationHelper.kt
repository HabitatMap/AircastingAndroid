package io.lunarlogic.aircasting.lib

import android.R
import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.content.Context
import android.os.Handler


class AuthenticationHelper(
    private val mContext: Context
) {

    fun getAuthToken(): String? {
        val accountManager = AccountManager.get(mContext)
        val accountType = "AirCastingAndroid"
        val accounts = accountManager.getAccountsByType(accountType)
        if (accounts.isEmpty()) {
            return null
        }
        val authToken = accountManager.peekAuthToken(accounts.first(), "oauth")
        return authToken
    }

    fun getEmail(): String? {
        val accountManager = AccountManager.get(mContext)
        val accountType = "AirCastingAndroid"
        val accounts = accountManager.getAccountsByType(accountType)

        if (accounts.isEmpty()) {
            return null
        }

        return accounts.first().name // TODO: dont know whats gonna happen when we log in with different account that the one we have on phone
    }

    fun removeAccount() {
        val accountManager = AccountManager.get(mContext)
        val accountType: String = "AirCastingAndroid"
        val accounts = accountManager.getAccountsByType(accountType)

        if (accounts.isEmpty()) {
            return
        }

        accountManager.removeAccount(accounts.first(), AccountManagerCallback {}, Handler())
    }
}
