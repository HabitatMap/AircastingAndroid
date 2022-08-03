package pl.llp.aircasting.ui.view.screens.create_account

import pl.llp.aircasting.data.api.response.CreateAccountErrorResponse
import pl.llp.aircasting.ui.view.common.ObservableViewMvc

interface CreateAccountViewMvc : ObservableViewMvc<CreateAccountViewMvc.Listener> {
    interface Listener {
        fun onCreateAccountClicked(
            profile_name: String,
            password: String,
            email: String,
            send_emails: Boolean
        )

        fun onLoginClicked()
    }

    fun showErrors(errorResponse: CreateAccountErrorResponse)
}
