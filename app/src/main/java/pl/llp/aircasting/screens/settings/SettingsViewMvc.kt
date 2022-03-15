package pl.llp.aircasting.screens.settings

import pl.llp.aircasting.screens.common.ObservableViewMvc

interface SettingsViewMvc : ObservableViewMvc<SettingsViewMvc.Listener> {

    interface BackendSettingsDialogListener{
        fun confirmClicked(urlValue: String, portValue: String)
    }

    interface MicrophoneSettingsDialogListener {
        fun confirmMicrophoneSettingsClicked(calibration: Int)
    }

    interface Listener {
        fun onMyAccountClicked()
        fun onBackendSettingsClicked()
        fun onThemeTextClicked()
        fun onToggleKeepScreenOnEnabled()
        fun onToggle24hourFormatEnabled()
        fun onToggleCrowdMapEnabled()
        fun onToggleMapsEnabled()
        fun onClearSDCardClicked()
        fun onMicrophoneSettingsClicked()
        fun yourPrivacyClicked()
    }
}
