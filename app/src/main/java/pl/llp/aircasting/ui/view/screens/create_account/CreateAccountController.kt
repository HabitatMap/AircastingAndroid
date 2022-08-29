package pl.llp.aircasting.ui.view.screens.create_account

import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.response.CreateAccountErrorResponse
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.ui.view.screens.login.LogOutInBackgroundInfoDisplayer
import pl.llp.aircasting.ui.view.screens.login.LoginActivity
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.extensions.showToast

class CreateAccountController(
    private val mContextActivity: AppCompatActivity,
    private val mViewMvc: CreateAccountViewMvcImpl,
    private val mSettings: Settings,
    private val mApiServiceFactory: ApiServiceFactory,
    private val fromOnboarding: Boolean?
) : CreateAccountViewMvc.Listener, LogOutInBackgroundInfoDisplayer {
    private val mErrorHandler = ErrorHandler(mContextActivity)
    private val mCreateAccountService =
        CreateAccountService(mSettings, mErrorHandler, mApiServiceFactory)

    fun onStart() {
        mViewMvc.registerListener(this)
        EventBus.getDefault().safeRegister(this)
    }

    fun onStop() {
        mViewMvc.unregisterListener(this)
        EventBus.getDefault().unregister(this)
    }

    override fun onCreateAccountClicked(
        profile_name: String,
        password: String,
        email: String,
        send_emails: Boolean
    ) {
        val successCallback = {
            MainActivity.start(mContextActivity)
        }
        val message = mContextActivity.getString(R.string.create_account_errors_message)
        val errorCallback = { errorResponse: CreateAccountErrorResponse ->
            mViewMvc.showErrors(errorResponse)
            mContextActivity.showToast(message, Toast.LENGTH_LONG)
        }
        mCreateAccountService.performCreateAccount(
            profile_name,
            password,
            email,
            send_emails,
            successCallback,
            errorCallback
        )
    }

    override fun onLoginClicked() {
        LoginActivity.start(mContextActivity, true, fromOnboarding)
    }

    override val infoView: View?
        get() = mViewMvc.rootView?.findViewById(R.id.logout_events_in_progress)
    override val button: Button?
        get() = mViewMvc.rootView?.findViewById(R.id.create_account_button)
}
