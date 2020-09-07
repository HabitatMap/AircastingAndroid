package io.lunarlogic.aircasting.screens.create_account

import io.lunarlogic.aircasting.networking.responses.CreateAccountErrorResponse
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc


interface CreateAccountViewMvc : ObservableViewMvc<CreateAccountViewMvc.Listener> {
    interface Listener {
        fun onCreateAccountClicked(username: String, password: String, email: String, send_emails: Boolean)
        fun onLoginClicked()
    }

    fun showError(errorResponse: CreateAccountErrorResponse)
}
