package pl.llp.aircasting.screens.login

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.networking.services.ApiServiceFactory
import pl.llp.aircasting.networking.services.ForgotPasswordService
import pl.llp.aircasting.screens.create_account.CreateAccountActivity
import pl.llp.aircasting.screens.main.MainActivity

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

    override fun onLoginClicked(profile_name: String, password: String) {
        val successCallback = {
            MainActivity.start(mContextActivity)
        }
        val message = mContextActivity.getString(R.string.invalid_credentials_message)
        val errorCallback = {
            mViewMvc.showError()
            val toast = Toast.makeText(mContextActivity, message, Toast.LENGTH_LONG)
            toast.show()
        }
        mLoginService.performLogin(profile_name, password, successCallback, errorCallback)
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
