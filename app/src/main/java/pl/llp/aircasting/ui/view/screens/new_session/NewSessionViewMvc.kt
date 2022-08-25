package pl.llp.aircasting.ui.view.screens.new_session

import pl.llp.aircasting.ui.view.common.ViewMvc

interface NewSessionViewMvc : ViewMvc {
    interface TurnOnWifiDialogListener {
        fun turnOnWifiClicked()
    }
}
