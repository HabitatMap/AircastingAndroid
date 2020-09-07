package io.lunarlogic.aircasting.screens.create_account

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import com.google.android.material.textfield.TextInputLayout
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.networking.responses.CreateAccountErrorResponse
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class CreateAccountViewMvcImpl : BaseObservableViewMvc<CreateAccountViewMvc.Listener>, CreateAccountViewMvc {
    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.activity_create_account, parent, false)

        val createAccountButton = rootView?.findViewById<Button>(R.id.create_account_button)
        createAccountButton?.setOnClickListener {
            onCreateAccountClicked()
        }

        val loginButton = rootView?.findViewById<Button>(R.id.sign_in_button)
        loginButton?.setOnClickListener {
            onLoginClicked()
        }
    }

    private fun onCreateAccountClicked() {
        val username = getEditTextValue(R.id.username_input)
        val password = getEditTextValue(R.id.password_input)
        val email = getEditTextValue(R.id.email_input)
        val send_emails = findViewById<CheckBox>(R.id.send_emails_input).isChecked

        for (listener in listeners) {
            listener.onCreateAccountClicked(username, password, email, send_emails)
        }
    }

    private fun onLoginClicked() {
        for (listener in listeners) {
            listener.onLoginClicked()
        }
    }

    override fun showError(errorRespose: CreateAccountErrorResponse) {
        val emailInputLayout = findViewById<TextInputLayout>(R.id.email)
        val usernameInputLayout = findViewById<TextInputLayout>(R.id.username)
        val passwordInputLayout = findViewById<TextInputLayout>(R.id.password)
        if(errorRespose.email != null && !errorRespose.email.isEmpty()) {
            emailInputLayout.error = errorRespose.email.first()
        }
        if(errorRespose.username != null && !errorRespose.username.isEmpty()) {
            usernameInputLayout.error = errorRespose.username.first()
        }

        if(errorRespose.password != null && !errorRespose.password.isEmpty()) {
            passwordInputLayout.error = errorRespose.password.first()
        }
    }
}
