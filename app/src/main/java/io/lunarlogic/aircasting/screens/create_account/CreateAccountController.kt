package io.lunarlogic.aircasting.screens.create_account

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.responses.CreateAccountErrorResponse
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.screens.main.MainActivity
import io.lunarlogic.aircasting.screens.new_session.LoginActivity

class CreateAccountController(
    private val mContextActivity: AppCompatActivity,
    private val mViewMvc: CreateAccountViewMvcImpl,
    private val mSettings: Settings,
    private val mApiServiceFactory: ApiServiceFactory,
    private val fromOnboarding: Boolean?
) : CreateAccountViewMvc.Listener {
    private val mErrorHandler = ErrorHandler(mContextActivity)
    private val mCreateAccountService = CreateAccountService(mSettings, mErrorHandler, mApiServiceFactory, mContextActivity)

    fun onStart() {
        mViewMvc.registerListener(this)
    }

    fun onStop() {
        mViewMvc.unregisterListener(this)
    }

    override fun onCreateAccountClicked(profile_name: String, password: String, email: String, send_emails: Boolean) {
        val successCallback = {
            MainActivity.start(mContextActivity)
        }
        val message =  mContextActivity.getString(R.string.create_account_errors_message)
        val errorCallback = { errorResponse: CreateAccountErrorResponse ->
            mViewMvc.showErrors(errorResponse)
            val toast = Toast.makeText(mContextActivity, message, Toast.LENGTH_LONG)
            toast.show()
        }
        mCreateAccountService.performCreateAccount(profile_name, password, email, send_emails, successCallback, errorCallback)
    }

    override fun onLoginClicked() {
        LoginActivity.start(mContextActivity, true, fromOnboarding)
    }
}
