package pl.llp.aircasting.ui.view.screens.login

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.ForgotPasswordService
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.ui.view.screens.create_account.CreateAccountActivity
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.extensions.hideKeyboard
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.extensions.showToast


@AssistedFactory
interface LoginControllerFactory {
    fun create(
        mContextActivity: AppCompatActivity,
        mViewMvc: LoginViewMvc,
        fragmentManager: FragmentManager
    ): LoginController
}

class LoginController @AssistedInject constructor(
    @Assisted private val mContextActivity: AppCompatActivity,
    @Assisted private val mViewMvc: LoginViewMvc,
    @Assisted private val fragmentManager: FragmentManager,
    private val mSettings: Settings,
    private val mLoginService: LoginService,
    private val mForgotPasswordService: ForgotPasswordService,
) : LoginViewMvc.Listener,
    LoginViewMvc.ForgotPasswordDialogListener,
    LogOutInBackgroundInfoDisplayer {

    fun onStart() {
        mViewMvc.registerListener(this)
        EventBus.getDefault().safeRegister(this)
    }

    fun onStop() {
        mViewMvc.unregisterListener(this)
        EventBus.getDefault().unregister(this)
    }

    override fun onLoginClicked(profileName: String, password: String) {
        mContextActivity.hideKeyboard()
        mContextActivity.lifecycleScope.launch {
            mLoginService.performLogin(profileName, password)
                .onSuccess {
                    Log.d(this@LoginController.TAG, "Saving login to settings")
                    mSettings.login(
                        it.username,
                        it.email,
                        it.authenticationToken,
                        it.sessionStoppedAlert
                    )
                    MainActivity.start(mContextActivity)
                }
                .onFailure {
                    mViewMvc.showError()
                    val message = mContextActivity.getString(R.string.invalid_credentials_message)
                    mContextActivity.showToast(message, Toast.LENGTH_LONG)
                }
        }
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
        mContextActivity.lifecycleScope.launch {
            mForgotPasswordService.resetPassword(emailValue)
                .onSuccess {
                    mContextActivity.showToast(
                        mContextActivity.getString(R.string.reset_email_sent),
                        Toast.LENGTH_LONG
                    )
                }
                .onFailure {
                    mContextActivity.showToast(
                        mContextActivity.getString(R.string.errors_network_forgot_password),
                        Toast.LENGTH_LONG
                    )
                }
        }
    }

    override val infoView: View?
        get() = mViewMvc.rootView?.findViewById(R.id.logout_events_in_progress)
    override val button: Button?
        get() = mViewMvc.rootView?.findViewById(R.id.login_button)
}
