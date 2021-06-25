package pl.llp.aircasting.screens.new_session

import pl.llp.aircasting.screens.common.ViewMvc


interface NewSessionViewMvc: ViewMvc {
    interface TurnOnWifiDialogListener {
        fun turnOnWifiClicked()
    }
}
