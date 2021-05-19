package io.lunarlogic.aircasting.authentication

import android.Manifest
import android.R
import android.accounts.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import io.lunarlogic.aircasting.screens.new_session.LoginActivity


class Authenticator(context: Context) : AbstractAccountAuthenticator(context) {
    var mContext: Context

    // Editing properties is not supported
    override fun editProperties(r: AccountAuthenticatorResponse, s: String): Bundle {
        throw UnsupportedOperationException()
    }

    // Don't add additional accounts
    override fun addAccount(
        response: AccountAuthenticatorResponse,
        accountType: String,
        authTokenType: String,
        requiredFeatures: Array<String>,
        options: Bundle
    ): Bundle {
        val am = AccountManager.get(mContext)
        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.GET_ACCOUNTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Checking to see if you can have a look at accounts present on the device.
            Log.d("Authenticator", "GET_ACCOUNTS not present.")
        }
        // TODO: I have to keep this in mind- using AccountManager by default i support one account <?> what about relogging
//        if (UserAccountUtil.getAccount(mContext) != null) {
//            // This means there's an account present already. If you don't want to support multiple accounts, keep this.
//            // This is how you report an error occurred.
//            val result = Bundle()
//            result.putInt(AccountManager.KEY_ERROR_CODE, 400)
//            result.putString(
//                AccountManager.KEY_ERROR_MESSAGE,
//                mContext.getResources().getString("only one account allowed")
//            )
//            return result
//        }
        val intent = Intent(mContext, LoginActivity::class.java)

        // This key can be anything. Try to use your domain/package
        intent.putExtra("io.lunarlogic.aircasting", accountType)

        // This key can be anything too. It's just a way of identifying the token's type (used when there are multiple permissions)
        intent.putExtra("full_access", authTokenType)

        // This key can be anything too. Used for your reference. Can skip it too.
        intent.putExtra("is_adding_new_account", true)

        // Copy this exactly from the line below.
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    // Ignore attempts to confirm credentials
    override fun confirmCredentials(
        r: AccountAuthenticatorResponse,
        account: Account,
        bundle: Bundle
    ): Bundle? = null

    // Implement this method if you want to save authToken with the account, great for using the inbuilt sync functionality.
    @Throws(NetworkErrorException::class)
    override fun getAuthToken(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String,
        bundle: Bundle
    ): Bundle {
        val am = AccountManager.get(mContext)
        var authToken = am.peekAuthToken(account, authTokenType)
//        if (TextUtils.isEmpty(authToken)) {
//            authToken = HTTPNetwork.login(account.name, am.getPassword(account))
//        }
        if (!TextUtils.isEmpty(authToken)) {
            val result = Bundle()
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
            return result
        }

        // If you reach here, person needs to login again. or sign up

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity which is the AccountsActivity in my case.
        val intent = Intent(mContext, LoginActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        intent.putExtra("io.lunarlogic.aircasting", account.type)
        intent.putExtra("full_access", authTokenType)
        val retBundle = Bundle()
        retBundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return retBundle
    }

    // Getting a label for the auth token is not supported
    override fun getAuthTokenLabel(authTokenType: String): String {
        throw UnsupportedOperationException()
    }

    // Updating user credentials is not supported
    override fun updateCredentials(
        r: AccountAuthenticatorResponse,
        account: Account,
        s: String,
        bundle: Bundle
    ): Bundle {
        throw UnsupportedOperationException()
    }

    // Checking features for the account is not supported
    override fun hasFeatures(
        r: AccountAuthenticatorResponse,
        account: Account,
        strings: Array<String>
    ): Bundle {
        throw UnsupportedOperationException()
    }

    // Handle a user logging out here.
    override fun getAccountRemovalAllowed(
        response: AccountAuthenticatorResponse,
        account: Account
    ): Bundle {
        return super.getAccountRemovalAllowed(response, account)
    }

    // Simple constructor
    init {
        mContext = context
    }
}
