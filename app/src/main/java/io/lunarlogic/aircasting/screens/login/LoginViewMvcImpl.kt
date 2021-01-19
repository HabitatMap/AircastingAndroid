package io.lunarlogic.aircasting.screens.new_session

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.textfield.TextInputLayout
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import kotlinx.android.synthetic.main.activity_login.view.*

class LoginViewMvcImpl : BaseObservableViewMvc<LoginViewMvc.Listener>, LoginViewMvc {
    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.activity_login, parent, false)

        rootView?.login_button?.setOnClickListener {
            onLoginClicked()
        }

        rootView?.create_account_button?.setOnClickListener {
            onCreateAccountClicked()
        }

        rootView?.forgot_password_button?.setOnClickListener {
            onForgotPasswordClicked()
        }
    }

    private fun onLoginClicked() {
        val username = getEditTextValue(R.id.username_input)
        val password = getEditTextValue(R.id.password_input)

        for (listener in listeners) {
            listener.onLoginClicked(username, password)
        }
    }

    private fun onForgotPasswordClicked() {
        for (listener in listeners) {
            listener.onForgotPasswordClicked()
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
