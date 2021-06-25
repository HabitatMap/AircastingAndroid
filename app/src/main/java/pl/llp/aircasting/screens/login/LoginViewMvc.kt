package pl.llp.aircasting.screens.new_session

import pl.llp.aircasting.screens.common.ObservableViewMvc


interface LoginViewMvc : ObservableViewMvc<LoginViewMvc.Listener> {
    interface ForgotPasswordDialogListener {
        fun confirmClicked(emailValue: String)
    }

    interface Listener {
        fun onLoginClicked(profile_name: String, password: String)
        fun onForgotPasswordClicked()
        fun onCreateAccountClicked()
    }

    fun showError()
}
