package pl.llp.aircasting.screens.create_account

import pl.llp.aircasting.networking.responses.CreateAccountErrorResponse
import pl.llp.aircasting.screens.common.ObservableViewMvc


interface CreateAccountViewMvc : ObservableViewMvc<CreateAccountViewMvc.Listener> {
    interface Listener {
        fun onCreateAccountClicked(profile_name: String, password: String, email: String, send_emails: Boolean)
        fun onLoginClicked()
    }

    fun showErrors(errorResponse: CreateAccountErrorResponse)
}
