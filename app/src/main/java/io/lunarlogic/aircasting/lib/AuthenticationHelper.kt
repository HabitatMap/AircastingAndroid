package io.lunarlogic.aircasting.lib

import android.R
import android.accounts.AccountManager
import android.accounts.AuthenticatorException
import android.accounts.OperationCanceledException
import android.content.Context
import android.os.Bundle
import java.io.IOException


class AuthenticationHelper {
    companion object {
        fun getAuthToken(context: Context): String? {
            val accountManager = AccountManager.get(context)
            val accountType: String = "AirCastingAndroid"
            val accounts = accountManager.getAccountsByType(accountType)
            if (accounts.isEmpty()) {
                return null
            }

            /* This method retrieves the token for the given account but doesn't pop the Authentication Page if not found */
            val authToken = accountManager.peekAuthToken(accounts.first(), "oauth") //TODO: what is the authTokenType???
            return authToken
            //return bundle.getString(AccountManager.KEY_AUTHTOKEN)
        }
    }
}
