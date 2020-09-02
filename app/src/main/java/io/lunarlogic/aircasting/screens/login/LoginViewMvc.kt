package io.lunarlogic.aircasting.screens.new_session

import com.google.android.material.textfield.TextInputLayout
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc


interface LoginViewMvc : ObservableViewMvc<LoginViewMvc.Listener> {
    interface Listener {
        fun onLoginClicked(username: String, password: String, usernameInputLayout: TextInputLayout, passwordInputLayout: TextInputLayout)
    }
}
