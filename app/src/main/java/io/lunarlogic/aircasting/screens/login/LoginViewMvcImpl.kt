package io.lunarlogic.aircasting.screens.new_session

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
        val username = getUsername()
        val password = getPassword()

        for (listener in listeners) {
            listener.onLoginClicked(username, password)
        }
    }

    private fun getUsername(): String {
        return rootView?.findViewById<EditText>(R.id.username)?.text.toString()
    }

    private fun getPassword(): String {
        return rootView?.findViewById<EditText>(R.id.password)?.text.toString()
    }
}