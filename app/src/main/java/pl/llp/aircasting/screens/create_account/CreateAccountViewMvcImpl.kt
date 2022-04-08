package pl.llp.aircasting.screens.create_account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import com.google.android.material.textfield.TextInputLayout
import pl.llp.aircasting.R
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.networking.responses.CreateAccountErrorResponse
import pl.llp.aircasting.screens.common.BaseObservableViewMvc

class CreateAccountViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    settings: Settings,
    fromOnboarding: Boolean?
) : BaseObservableViewMvc<CreateAccountViewMvc.Listener>(), CreateAccountViewMvc {
    init {
        this.rootView = inflater.inflate(R.layout.activity_create_account, parent, false)
        val createAccountButton = rootView?.findViewById<Button>(R.id.create_account_button)
        createAccountButton?.setOnClickListener {
            onCreateAccountClicked()
        }
        val loginButton = rootView?.findViewById<Button>(R.id.sign_in_button)
        loginButton?.setOnClickListener {
            onLoginClicked()
        }
        val progressBarFrame = rootView?.findViewById<FrameLayout>(R.id.progress_bar_frame)
        if (fromOnboarding == true) {
            progressBarFrame?.visibility = View.VISIBLE
        } else {
            progressBarFrame?.visibility = View.GONE
        }
    }

    private fun onCreateAccountClicked() {
        val profile_name = getEditTextValue(R.id.profile_name_input)
        val password = getEditTextValue(R.id.password_input)
        val email = getEditTextValue(R.id.email_input)
        val send_emails = findViewById<CheckBox>(R.id.send_emails_input).isChecked

        for (listener in listeners) {
            listener.onCreateAccountClicked(profile_name, password, email, send_emails)
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
