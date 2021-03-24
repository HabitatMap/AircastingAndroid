package io.lunarlogic.aircasting.screens.new_session

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.ForgotPasswordService
import io.lunarlogic.aircasting.screens.create_account.CreateAccountActivity
import io.lunarlogic.aircasting.screens.login.ForgotPasswordDialog
import io.lunarlogic.aircasting.screens.main.MainActivity
import io.lunarlogic.aircasting.screens.login.LoginService

class  LoginController(
    private val mContextActivity: AppCompatActivity,
    private val mViewMvc: LoginViewMvc,
    private val mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    private val fragmentManager: FragmentManager
) : LoginViewMvc.Listener,
    LoginViewMvc.ForgotPasswordDialogListener {
    private val mErrorHandler = ErrorHandler(mContextActivity)
    private val mLoginService = LoginService(mSettings, mErrorHandler, mApiServiceFactory)
    private val mForgotPasswordService = ForgotPasswordService(mContextActivity, mErrorHandler, mApiServiceFactory)

    fun onStart() {
        mViewMvc.registerListener(this)
    }

    fun onStop() {
        mViewMvc.unregisterListener(this)
    }

    override fun onLoginClicked(username: String, password: String) {
        val successCallback = {
            MainActivity.start(mContextActivity)
        }
        val message = mContextActivity.getString(R.string.invalid_credentials_message)
        val errorCallback = {
            mViewMvc.showError()
            val toast = Toast.makeText(mContextActivity, message, Toast.LENGTH_LONG)
            toast.show()
        }
        mLoginService.performLogin(username, password, successCallback, errorCallback)
    }

    override fun onForgotPasswordClicked() {
        startForgotPasswordDialog()
    }

    override fun onCreateAccountClicked() {
        CreateAccountActivity.start(mContextActivity)
    }

    private fun startForgotPasswordDialog() {
        ForgotPasswordDialog(fragmentManager, this).show()
    }

    override fun confirmClicked(emailValue: String) {
        mForgotPasswordService.resetPassword(emailValue)
    }
}
