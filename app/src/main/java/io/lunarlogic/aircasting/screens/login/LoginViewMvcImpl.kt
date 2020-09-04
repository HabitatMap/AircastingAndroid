package io.lunarlogic.aircasting.screens.new_session

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.textfield.TextInputLayout
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

        val createAccountButton = rootView?.findViewById<Button>(R.id.create_account_button)
        createAccountButton?.setOnClickListener {
            onCreateAccountClicked()
        }
    }

    private fun onLoginClicked() {
        val username = getEditTextValue(R.id.username_input)
        val password = getEditTextValue(R.id.password_input)

        for (listener in listeners) {
            listener.onLoginClicked(username, password)
        }
    }

    private fun onCreateAccountClicked() {
        for (listener in listeners) {
            listener.onCreateAccountClicked()
        }
    }

    override fun showError() {
        val usernameInputLayout = findViewById<TextInputLayout>(R.id.username)
        val passwordInputLayout = findViewById<TextInputLayout>(R.id.password)
        usernameInputLayout.error = " "
        passwordInputLayout.error = " "
    }
}
