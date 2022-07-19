package pl.llp.aircasting.ui.view.screens.create_account

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import com.google.android.material.textfield.TextInputLayout
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.response.CreateAccountErrorResponse
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.extensions.hideKeyboard
import java.util.*

class CreateAccountViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    settings: Settings,
    fromOnboarding: Boolean?
) : BaseObservableViewMvc<CreateAccountViewMvc.Listener>(), CreateAccountViewMvc {
    init {
        this.rootView = inflater.inflate(R.layout.activity_create_account, parent, false)
        val context = rootView?.context

        val createAccountButton = rootView?.findViewById<Button>(R.id.create_account_button)
        createAccountButton?.setOnClickListener {
            onCreateAccountClicked(context, it)
        }
        val loginButton = rootView?.findViewById<Button>(R.id.sign_in_button)
        loginButton?.setOnClickListener {
            onLoginClicked(context, it)
        }
        val progressBarFrame = rootView?.findViewById<FrameLayout>(R.id.progress_bar_frame)
        if (fromOnboarding == true) {
            progressBarFrame?.visibility = View.VISIBLE
        } else {
            progressBarFrame?.visibility = View.GONE
        }
    }

    private fun onCreateAccountClicked(context: Context?, view: View) {
        context?.hideKeyboard(view)
        val profile_name = getEditTextValue(R.id.profile_name_input)
        val password = getEditTextValue(R.id.password_input)
        val email = getEditTextValue(R.id.email_input)
        val send_emails = findViewById<CheckBox>(R.id.send_emails_input).isChecked

        for (listener in listeners) {
            listener.onCreateAccountClicked(profile_name, password, email, send_emails)
        }
    }

    private fun onLoginClicked(context: Context?, it: View) {
        context?.hideKeyboard(it)
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
            val inputLayout: TextInputLayout = findViewById(it!!)
            val errors: List<String>? =
                errorRespose.javaClass.getMethod("get" + inputLayoutName.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }).invoke(errorRespose) as? List<String>

            if (errors != null && !errors.isEmpty()) {
                inputLayout.error = errors.joinToString(separator = ". ")
            } else {
                inputLayout.error = null
            }
        }

    }
}
