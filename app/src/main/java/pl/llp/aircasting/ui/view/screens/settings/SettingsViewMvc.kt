package pl.llp.aircasting.ui.view.screens.settings

import pl.llp.aircasting.ui.view.common.ObservableViewMvc

interface SettingsViewMvc : ObservableViewMvc<SettingsViewMvc.Listener> {

    interface BackendSettingsDialogListener{
        fun confirmClicked(urlValue: String, portValue: String)
    }

    interface MicrophoneSettingsDialogListener {
        fun confirmMicrophoneSettingsClicked(calibration: Int)
    }

    interface Listener {
        fun onMyAccountClicked()
        fun onToggleThemeChange()
        fun onBackendSettingsClicked()
        fun onToggleKeepScreenOnEnabled()
        fun onToggle24hourFormatEnabled()
        fun onToggleCelsiusScaleEnabled()
        fun onToggleCrowdMapEnabled()
        fun onToggleDormantStreamAlert(enabled: Boolean)
        fun onToggleMapsEnabled()
        fun onToggleSatelliteViewEnabled()
        fun onClearSDCardClicked()
        fun onMicrophoneSettingsClicked()
        fun yourPrivacyClicked()
    }
}
