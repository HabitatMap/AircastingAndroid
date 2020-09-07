package io.lunarlogic.aircasting.screens.create_account

import android.content.Context
import android.widget.Toast
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.responses.CreateAccountErrorResponse
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.MobileSessionsSyncService
import io.lunarlogic.aircasting.screens.main.MainActivity
import io.lunarlogic.aircasting.screens.new_session.LoginActivity

class CreateAccountController(
    private val mContext: Context,
    private val mViewMvc: CreateAccountViewMvcImpl,
    private val mSettings: Settings
) : CreateAccountViewMvc.Listener {
    private val mErrorHandler = ErrorHandler(mContext)
    private val mCreateAccountService = CreateAccountService(mSettings, mErrorHandler)

    fun onStart() {
        mViewMvc.registerListener(this)
    }

    fun onStop() {
        mViewMvc.unregisterListener(this)
    }

    override fun onCreateAccountClicked(username: String, password: String, email: String, send_emails: Boolean) {
        print("trying to create account")
        val successCallback = {
            MainActivity.start(mContext)
        }
        val message = "Something wrong with the credentials"
        val errorCallback = { errorResponse: CreateAccountErrorResponse ->
            mViewMvc.showError(errorResponse)
            println("przekazany error: "+errorResponse.email)
            val toast = Toast.makeText(mContext, message, Toast.LENGTH_LONG)
            toast.show()
        }
        mCreateAccountService.performCreateAccount(username, password, email, send_emails, successCallback, errorCallback)
    }

    override fun onLoginClicked() {
        LoginActivity.start(mContext)
    }
}
