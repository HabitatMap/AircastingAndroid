package io.lunarlogic.aircasting.screens.settings

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.new_session.LoginViewMvc

interface SettingsViewMvc : ObservableViewMvc<SettingsViewMvc.Listener> {

    //TODO: maybe i should make some interface like 'BackendSettingsViewMvc and place below interface there?:
    interface BackendSettingsDialogListener{
        fun confirmClicked(urlValue: String, portValue: String)
    }

    interface MicrophoneSettingsDialogListener {
        fun confirmMicrophoneSettingsClicked(calibration: Int)
    }

    interface Listener {
        fun onMyAccountClicked()
        fun onBackendSettingsClicked()
        fun onToggleCrowdMapEnabled()
        fun onToggleMapsEnabled()
        fun onMicrophoneSettingsClicked()
    }
}
