package io.lunarlogic.aircasting.screens.new_session

import android.content.Context
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.main.MainActivity
import io.lunarlogic.aircasting.screens.login.LoginService

class LoginController(
    private val mContext: Context,
    private val mViewMvc: LoginViewMvc,
    settings: Settings
) : LoginViewMvc.Listener {
    val loginService = LoginService(settings)

    fun onStart() {
        mViewMvc.registerListener(this)
    }

    fun onStop() {
        mViewMvc.unregisterListener(this)
    }

    override fun onLoginClicked(username: String, password: String) {
        val successCallback = { MainActivity.start(mContext) }
        loginService.performLogin(username, password, successCallback)
    }
}