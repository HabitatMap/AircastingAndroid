package io.lunarlogic.aircasting.screens.settings.clear_sd_card.my_account

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface MyAccountViewMvc : ObservableViewMvc<MyAccountViewMvc.Listener> {

    interface Listener{
        fun onSignOutClicked()
    }

    fun bindAccountDetail(email : String?)
}
