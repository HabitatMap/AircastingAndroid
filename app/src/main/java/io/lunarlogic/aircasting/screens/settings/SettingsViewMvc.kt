package io.lunarlogic.aircasting.screens.settings

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.new_session.LoginViewMvc

interface SettingsViewMvc : ObservableViewMvc<SettingsViewMvc.Listener> {

    interface BackendSettingsDialogListener{
        fun confirmClicked(urlValue: String, portValue: String)
    }

    interface Listener {
        fun onMyAccountClicked()
        fun onBackendSettingsClicked()
        fun onToggleCrowdMapEnabled()
        fun onToggleMapsEnabled()
    }
}
