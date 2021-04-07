package io.lunarlogic.aircasting.screens.new_session

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.textfield.TextInputLayout
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import kotlinx.android.synthetic.main.activity_login.view.*

class LoginViewMvcImpl : BaseObservableViewMvc<LoginViewMvc.Listener>, LoginViewMvc {
    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        settings: Settings,
        fromOnboarding: Boolean?): super() {
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

        val progressBarFrame = rootView?.findViewById<FrameLayout>(R.id.progress_bar_frame)
        if (fromOnboarding == true) {
            progressBarFrame?.visibility = View.VISIBLE
        } else {
            progressBarFrame?.visibility = View.GONE
        }
    }

    private fun onLoginClicked() {
        val profile_name = getEditTextValue(R.id.profile_name_input)
        val password = getEditTextValue(R.id.password_input)

        for (listener in listeners) {
            listener.onLoginClicked(profile_name, password)
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
        val profilenameInputLayout = findViewById<TextInputLayout>(R.id.profile_name)
        val passwordInputLayout = findViewById<TextInputLayout>(R.id.password)
        profilenameInputLayout.error = " "
        passwordInputLayout.error = " "
    }
}
