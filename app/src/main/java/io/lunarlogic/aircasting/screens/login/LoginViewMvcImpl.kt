package io.lunarlogic.aircasting.screens.new_session

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class LoginViewMvcImpl : BaseObservableViewMvc<LoginViewMvc.Listener>, LoginViewMvc {
    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.activity_login, parent, false)

        val loginButton = rootView?.findViewById<Button>(R.id.login_button)
        loginButton?.setOnClickListener {
            onLoginClicked()
        }
    }

    private fun onLoginClicked() {
        val username = getEditTextValue(R.id.username)
        val password = getEditTextValue(R.id.password)

        for (listener in listeners) {
            listener.onLoginClicked(username, password)
        }
    }
}
