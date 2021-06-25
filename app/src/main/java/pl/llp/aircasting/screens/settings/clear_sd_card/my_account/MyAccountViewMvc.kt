package pl.llp.aircasting.screens.settings.clear_sd_card.my_account

import pl.llp.aircasting.screens.common.ObservableViewMvc

interface MyAccountViewMvc : ObservableViewMvc<MyAccountViewMvc.Listener> {

    interface Listener{
        fun onSignOutClicked()
    }

    fun bindAccountDetail(email : String?)
}
