package io.lunarlogic.aircasting.screens.new_session

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.screens.create_account.CreateAccountActivity
import io.lunarlogic.aircasting.screens.login.ForgotPasswordDialog
import io.lunarlogic.aircasting.screens.main.MainActivity
import io.lunarlogic.aircasting.screens.login.LoginService

class  LoginController(
    private val mContext: Context,
    private val mViewMvc: LoginViewMvc,
    mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    private val fragmentManager: FragmentManager
) : LoginViewMvc.Listener,
    LoginViewMvc.ForgotPasswordDialogListener{
    private val mErrorHandler = ErrorHandler(mContext)
    private val mLoginService = LoginService(mSettings, mErrorHandler, mApiServiceFactory)

    fun onStart() {
        mViewMvc.registerListener(this)
    }

    fun onStop() {
        mViewMvc.unregisterListener(this)
    }

    override fun onLoginClicked(username: String, password: String) {
        val successCallback = {
            MainActivity.start(mContext)
        }
        val message = mContext.getString(R.string.invalid_credentials_message)
        val errorCallback = {
            mViewMvc.showError()
            val toast = Toast.makeText(mContext, message, Toast.LENGTH_LONG)
            toast.show()
        }
        mLoginService.performLogin(username, password, successCallback, errorCallback)
    }

    override fun onForgotPasswordClicked() {
        startForgotPasswordDialog()
    }

    override fun onCreateAccountClicked() {
        CreateAccountActivity.start(mContext)
    }

    private fun startForgotPasswordDialog() {
        ForgotPasswordDialog(fragmentManager, this).show()
    }

    override fun confirmClicked(emailValue: String) {
        //TODO: tutaj zaczyna sie ta bardziej backendowa czesc- jakis event bus i call do api... cos tego czy inaczej??? gdzie mialbym ten Event wyslac?
    }
}
