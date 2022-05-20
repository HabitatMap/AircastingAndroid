package pl.llp.aircasting.ui.view.screens.settings.my_account

import pl.llp.aircasting.ui.view.common.ObservableViewMvc

interface MyAccountViewMvc : ObservableViewMvc<MyAccountViewMvc.Listener> {

    interface Listener{
        fun onSignOutClicked()
    }

    fun bindAccountDetail(email : String?)
}
