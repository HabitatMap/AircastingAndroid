package io.lunarlogic.aircasting.screens.new_session

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc


interface LoginViewMvc : ObservableViewMvc<LoginViewMvc.Listener> {
    interface Listener {
        fun onLoginClicked(username: String, password: String)
    }

    fun showError()
}
