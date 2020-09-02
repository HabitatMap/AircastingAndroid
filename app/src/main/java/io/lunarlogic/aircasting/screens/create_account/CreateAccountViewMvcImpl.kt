package io.lunarlogic.aircasting.screens.create_account

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.textfield.TextInputLayout
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.new_session.LoginViewMvc

class CreateAccountViewMvcImpl : BaseObservableViewMvc<LoginViewMvc.Listener>, LoginViewMvc {
    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.activity_create_account, parent, false)

        val createAccountButton = rootView?.findViewById<Button>(R.id.login_button)
        createAccountButton?.setOnClickListener {
            onCreateAccountClicked()
        }
    }

    private fun onCreateAccountClicked() {
        val username = getEditTextValue(R.id.username_input)
        val password = getEditTextValue(R.id.password_input)

        for (listener in listeners) {
            listener.onLoginClicked(username, password)
        }
    }

    override fun showError() {
        val usernameInputLayout = findViewById<TextInputLayout>(R.id.username)
        val passwordInputLayout = findViewById<TextInputLayout>(R.id.password)
        usernameInputLayout.error = " "
        passwordInputLayout.error = " "
    }
}
