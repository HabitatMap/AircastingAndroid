package io.lunarlogic.aircasting.screens.create_account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import com.google.android.material.textfield.TextInputLayout
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.responses.CreateAccountErrorResponse
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class CreateAccountViewMvcImpl : BaseObservableViewMvc<CreateAccountViewMvc.Listener>, CreateAccountViewMvc {
    constructor(
        inflater: LayoutInflater, parent: ViewGroup?, settings: Settings): super() {
        this.rootView = inflater.inflate(R.layout.activity_create_account, parent, false)

        val createAccountButton = rootView?.findViewById<Button>(R.id.create_account_button)
        createAccountButton?.setOnClickListener {
            onCreateAccountClicked()
        }

        val loginButton = rootView?.findViewById<Button>(R.id.sign_in_button)
        loginButton?.setOnClickListener {
            onLoginClicked()
        }

        if (settings.shouldOnboardingAppear()) {
            val progressBar = rootView?.findViewById<ProgressBar>(R.id.progress_bar)
            progressBar?.visibility = View.VISIBLE
            progressBar?.progress = 75   // todo: hardcoded for now <?>
//            settings.onboardingAccepted() //todo: this should be uncommented before finishing the task
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

    override fun showErrors(errorRespose: CreateAccountErrorResponse) {
        showError("email", errorRespose)
        showError("username", errorRespose)
        showError("password", errorRespose)
    }

    private fun showError(inputLayoutName: String, errorRespose: CreateAccountErrorResponse) {
        val inputId = rootView?.resources?.getIdentifier(inputLayoutName, "id", context.packageName)
        inputId.let {
            val inputLayout: TextInputLayout = findViewById<TextInputLayout>(it!!)
            val errors: List<String>? = errorRespose.javaClass.getMethod("get"+inputLayoutName.capitalize()).invoke(errorRespose) as? List<String>

            if(errors != null && !errors.isEmpty()) {
                inputLayout.error = errors.joinToString(separator = ". ")
            } else {
                inputLayout.error = null
            }
        }

    }
}
