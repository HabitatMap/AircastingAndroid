package pl.llp.aircasting.ui.view.screens.login

import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.services.ForgotPasswordService
import pl.llp.aircasting.ui.view.screens.create_account.CreateAccountActivity
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.hideKeyboard
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.extensions.showToast

class LoginController(
    private val mContextActivity: AppCompatActivity,
    private val mViewMvc: LoginViewMvc,
    private val mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    private val fragmentManager: FragmentManager
) : LoginViewMvc.Listener,
    LoginViewMvc.ForgotPasswordDialogListener,
    LogOutInBackgroundInfoDisplayer {
    private val mErrorHandler = ErrorHandler(mContextActivity)
    private val mLoginService = LoginService(mSettings, mErrorHandler, mApiServiceFactory)
    private val mForgotPasswordService =
        ForgotPasswordService(mContextActivity, mErrorHandler, mApiServiceFactory)

    fun onStart() {
        mViewMvc.registerListener(this)
        EventBus.getDefault().safeRegister(this)
    }

    fun onStop() {
        mViewMvc.unregisterListener(this)
        EventBus.getDefault().unregister(this)
    }

    override fun onLoginClicked(profile_name: String, password: String) {
        mContextActivity.hideKeyboard()
        val successCallback = {
            MainActivity.start(mContextActivity)
        }
        val message = mContextActivity.getString(R.string.invalid_credentials_message)
        val errorCallback = {
            mViewMvc.showError()
            mContextActivity.showToast(message, Toast.LENGTH_LONG)
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

    override val infoView: View?
        get() = mViewMvc.rootView?.findViewById(R.id.logout_events_in_progress)
    override val button: Button?
        get() = mViewMvc.rootView?.findViewById(R.id.login_button)
}
