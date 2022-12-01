package pl.llp.aircasting.ui.view.screens.create_account

import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.params.CreateAccountBody
import pl.llp.aircasting.data.api.params.CreateAccountParams
import pl.llp.aircasting.data.api.response.CreateAccountErrorResponse
import pl.llp.aircasting.data.api.response.UserResponse
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.ui.view.screens.login.LogOutInBackgroundInfoDisplayer
import pl.llp.aircasting.ui.view.screens.login.LoginActivity
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.InternalAPIError
import pl.llp.aircasting.util.exceptions.UnexpectedAPIError
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.extensions.showToast
import retrofit2.Response

class CreateAccountController(
    private val mContextActivity: AppCompatActivity,
    private val mViewMvc: CreateAccountViewMvcImpl,
    private val mSettings: Settings,
    private val apiService: ApiService,
    private val fromOnboarding: Boolean?,
    private val mErrorHandler: ErrorHandler = ErrorHandler(mContextActivity),
    private val coroutineScope: CoroutineScope = mContextActivity.lifecycleScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : CreateAccountViewMvc.Listener, LogOutInBackgroundInfoDisplayer {

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
        val createAccountParams = CreateAccountParams(
            profile_name,
            password,
            email,
            send_emails
        )
        val createAccountBody = CreateAccountBody(createAccountParams)

        callCreateAccountAndProcessResponse(apiService, createAccountBody)
    }

    private fun callCreateAccountAndProcessResponse(
        apiService: ApiService,
        createAccountBody: CreateAccountBody
    ) {
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            mErrorHandler.handleAndDisplay(UnexpectedAPIError(exception))
        }

        coroutineScope.launch(mainDispatcher + exceptionHandler) {
            val response = withContext(ioDispatcher) {
                apiService.createAccount(createAccountBody)
            }
            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    mSettings.login(
                        body.username,
                        body.email,
                        body.authenticationToken,
                        body.sessionStoppedAlert
                    )
                    MainActivity.start(mContextActivity)
                }
            } else if (response.code() == 422) {
                showFormErrors(response)
                showFormErrorsToast()
            } else {
                mErrorHandler.handleAndDisplay(InternalAPIError())
            }
        }
    }

    private fun showFormErrorsToast() {
        val message = mContextActivity.getString(R.string.create_account_errors_message)
        mContextActivity.showToast(message, Toast.LENGTH_LONG)
    }

    private fun showFormErrors(response: Response<UserResponse>) {
        val errorResponse =
            Gson().fromJson(response.errorBody()?.string(), CreateAccountErrorResponse::class.java)
        mViewMvc.showErrors(errorResponse)
    }

    override fun onLoginClicked() {
        LoginActivity.start(mContextActivity, true, fromOnboarding)
    }

    override val infoView: View?
        get() = mViewMvc.rootView?.findViewById(R.id.logout_events_in_progress)
    override val button: Button?
        get() = mViewMvc.rootView?.findViewById(R.id.create_account_button)
}
