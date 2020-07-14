package io.lunarlogic.aircasting.screens.new_session

import android.content.Context
import android.widget.Toast
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.MobileSessionsSyncService
import io.lunarlogic.aircasting.screens.main.MainActivity
import io.lunarlogic.aircasting.screens.login.LoginService

class LoginController(
    private val mContext: Context,
    private val mViewMvc: LoginViewMvc,
    private val mSettings: Settings
) : LoginViewMvc.Listener {
    private val mErrorHandler = ErrorHandler(mContext)
    private val mLoginService = LoginService(mSettings, mErrorHandler)

    fun onStart() {
        mViewMvc.registerListener(this)
    }

    fun onStop() {
        mViewMvc.unregisterListener(this)
    }

    override fun onLoginClicked(username: String, password: String) {
        val successCallback = {
            performSessionSync()
            MainActivity.start(mContext)
        }
        val message = mContext.getString(R.string.invalid_credentials_message)
        val errorCallback = {
            val toast = Toast.makeText(mContext, message, Toast.LENGTH_LONG)
            toast.show()
        }
        mLoginService.performLogin(username, password, successCallback, errorCallback)
    }

    private fun performSessionSync() {
        val apiService =  ApiServiceFactory.get(mSettings.getAuthToken()!!)
        val sessionSyncService = MobileSessionsSyncService(apiService, mErrorHandler)
        sessionSyncService.sync()
    }
}