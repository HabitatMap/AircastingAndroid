package pl.llp.aircasting.ui.view.screens.login

import pl.llp.aircasting.ui.view.common.ObservableViewMvc


interface LoginViewMvc : ObservableViewMvc<LoginViewMvc.Listener> {
    interface ForgotPasswordDialogListener {
        fun confirmClicked(emailValue: String)
    }

    interface Listener {
        fun onLoginClicked(profileName: String, password: String)
        fun onForgotPasswordClicked()
        fun onCreateAccountClicked()
    }

    fun showError()
}
