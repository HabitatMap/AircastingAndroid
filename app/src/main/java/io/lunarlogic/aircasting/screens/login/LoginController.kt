package io.lunarlogic.aircasting.screens.new_session

import android.content.Context
import android.widget.Toast
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.main.MainActivity
import io.lunarlogic.aircasting.screens.login.LoginService
import kotlinx.android.synthetic.main.fragment_airbeam_connected.view.*

class LoginController(
    private val mContext: Context,
    private val mViewMvc: LoginViewMvc,
    settings: Settings
) : LoginViewMvc.Listener {
    val loginService = LoginService(settings, ErrorHandler(mContext))

    fun onStart() {
        mViewMvc.registerListener(this)
    }

    fun onStop() {
        mViewMvc.unregisterListener(this)
    }

    override fun onLoginClicked(username: String, password: String) {
        val successCallback = { MainActivity.start(mContext) }
        val message = mContext.getString(R.string.invalid_credentials_message)
        val errorCallback = {
            val toast = Toast.makeText(mContext, message, Toast.LENGTH_LONG)
            toast.show()
        }
        loginService.performLogin(username, password, successCallback, errorCallback)
    }
}