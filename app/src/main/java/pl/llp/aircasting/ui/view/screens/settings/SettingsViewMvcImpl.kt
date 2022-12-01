package pl.llp.aircasting.ui.view.screens.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kotlinx.android.synthetic.main.fragment_settings.view.*
import pl.llp.aircasting.BuildConfig
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc
import pl.llp.aircasting.util.Settings

class SettingsViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    mSettings: Settings
) : BaseObservableViewMvc<SettingsViewMvc.Listener>(), SettingsViewMvc {

    init {
        this.rootView = inflater.inflate(R.layout.fragment_settings, parent, false)

        val myAccountButton = rootView?.myAccount_Button
        myAccountButton?.setOnClickListener {
            onMyAccountClicked()
        }
        val microphoneSettingsButton =
            rootView?.findViewById<Button>(R.id.microphone_settings_button)
        microphoneSettingsButton?.setOnClickListener {
            onMicrophoneSettingsClicked()
        }
        val themeChangeBtn = rootView?.theme_change
        themeChangeBtn?.isChecked = mSettings.isDarkThemeEnabled()
        themeChangeBtn?.setOnCheckedChangeListener { _, _ ->
            onToggleThemeChangeEnabled()
        }
        val keepScreenOnSwitch = rootView?.keep_screen_on_switch
        keepScreenOnSwitch?.isChecked = mSettings.isKeepScreenOnEnabled()
        keepScreenOnSwitch?.setOnCheckedChangeListener { _, _ ->
            onToggleKeepScreenOnEnabled()
        }
        val use24HourFormatSwitch = rootView?.use_24_hour_format_switch
        use24HourFormatSwitch?.isChecked = mSettings.isUsing24HourFormat()
        use24HourFormatSwitch?.setOnCheckedChangeListener { _, _ ->
            onToggleUse24HourFormatEnabled()
        }
        val useCelsiusScaleSwitch = rootView?.use_celcius_scale_switch
        useCelsiusScaleSwitch?.isChecked = mSettings.isCelsiusScaleEnabled()
        useCelsiusScaleSwitch?.setOnCheckedChangeListener { _, _ ->
            onToggleCelsiusScaleEnabled()
        }
        val contributeToCrowdMapSwitch = rootView?.crowd_map_settings_switch
        contributeToCrowdMapSwitch?.isChecked = mSettings.isCrowdMapEnabled()
        contributeToCrowdMapSwitch?.setOnCheckedChangeListener { _, _ ->
            onToggleCrowdMapEnabled()
        }
        val dormantStreamAlertSwitch = rootView?.dormant_stream_alert_settings_switch
        dormantStreamAlertSwitch?.isChecked = mSettings.isDormantStreamAlertEnabled()
        dormantStreamAlertSwitch?.setOnCheckedChangeListener { _, isChecked ->
            onToggleDormantStreamAlert(isChecked)
        }
        val mapEnabledSwitch = rootView?.map_settings_switch
        mapEnabledSwitch?.isChecked = mSettings.areMapsDisabled()
        mapEnabledSwitch?.setOnCheckedChangeListener { _, _ ->
            onToggleMapsEnabled()
        }
        val backendSettingsButton = rootView?.findViewById<Button>(R.id.backend_settings_button)
        backendSettingsButton?.setOnClickListener {
            onBackendSettingsClicked()
        }
        val clearSDCardButton = rootView?.findViewById<Button>(R.id.clear_sd_card_button)
        clearSDCardButton?.visibility = View.VISIBLE
        clearSDCardButton?.setOnClickListener {
            onClearSDCardClicked()
        }
        val yourPrivacyButton = rootView?.findViewById<Button>(R.id.your_privacy_button)
        yourPrivacyButton?.setOnClickListener {
            yourPrivacyClicked()
        }
        val versionValueTextView = rootView?.app_version_value_text_view
        versionValueTextView?.text = BuildConfig.VERSION_NAME

        val txtUsername = rootView?.txtUsername
        txtUsername?.text = mSettings.getProfileName()

        val satelliteSwitch = rootView?.use_satellite_view_switch
        satelliteSwitch?.isChecked = mSettings.isUsingSatelliteView()
        satelliteSwitch?.setOnCheckedChangeListener { _, _ ->
            onToggleSatelliteView()
        }
    }

    private fun onToggleThemeChangeEnabled() {
        for (listener in listeners) {
            listener.onToggleThemeChange()
        }
    }

    private fun onToggleKeepScreenOnEnabled() {
        for (listener in listeners) {
            listener.onToggleKeepScreenOnEnabled()
        }
    }

    private fun onToggleUse24HourFormatEnabled() {
        for (listener in listeners) {
            listener.onToggle24hourFormatEnabled()
        }
    }

    private fun onToggleCelsiusScaleEnabled() {
        for (listener in listeners) {
            listener.onToggleCelsiusScaleEnabled()
        }
    }

    private fun onMicrophoneSettingsClicked() {
        for (listener in listeners) {
            listener.onMicrophoneSettingsClicked()
        }
    }

    private fun onBackendSettingsClicked() {
        for (listener in listeners) {
            listener.onBackendSettingsClicked()
        }
    }

    private fun onToggleCrowdMapEnabled() {
        for (listener in listeners) {
            listener.onToggleCrowdMapEnabled()
        }
    }

    private fun onToggleDormantStreamAlert(enabled: Boolean) {
        for (listener in listeners) {
            listener.onToggleDormantStreamAlert(enabled)
        }
    }

    private fun onToggleMapsEnabled() {
        for (listener in listeners) {
            listener.onToggleMapsEnabled()
        }
    }

    private fun onToggleSatelliteView() {
        for (listener in listeners) {
            listener.onToggleSatelliteViewEnabled()
        }
    }

    private fun onClearSDCardClicked() {
        for (listener in listeners) {
            listener.onClearSDCardClicked()
        }
    }

    private fun onMyAccountClicked() {
        for (listener in listeners) {
            listener.onMyAccountClicked()
        }
    }

    private fun yourPrivacyClicked() {
        for (listener in listeners) {
            listener.yourPrivacyClicked()
        }
    }
}
