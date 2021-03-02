package io.lunarlogic.aircasting.screens.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.BuildConfig
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import kotlinx.android.synthetic.main.fragment_settings.view.*

class SettingsViewMvcImpl : BaseObservableViewMvc<SettingsViewMvc.Listener>, SettingsViewMvc  {

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?,
        mSettings: Settings
    ) : super(){
        this.rootView = inflater.inflate(R.layout.fragment_settings, parent, false)

        val myAccountButton = rootView?.myAccount_Button
        myAccountButton?.setOnClickListener {
            onMyAccountClicked()
        }

        val microphoneSettingsButton = rootView?.findViewById<Button>(R.id.microphone_settings_button)
        microphoneSettingsButton?.setOnClickListener {
            onMicrophoneSettingsClicked()
        }

        val contributeToCrowdMapSwitch = rootView?.crowd_map_settings_switch
        contributeToCrowdMapSwitch?.isChecked = mSettings.isCrowdMapEnabled()
        contributeToCrowdMapSwitch?.setOnCheckedChangeListener { _, _ ->
            onToggleCrowdMapEnabled()
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
        if (mSettings.airbeam3Connected()) {
            clearSDCardButton?.visibility = View.VISIBLE
            clearSDCardButton?.setOnClickListener {
                onClearSDCardClicked()
            }
        }


        val versionValueTextView = rootView?.app_version_value_text_view
        versionValueTextView?.text = BuildConfig.VERSION_NAME
    }

    private fun onMicrophoneSettingsClicked() {
        for (listener in listeners) {
            listener.onMicrophoneSettingsClicked()
        }
    }

    private fun onBackendSettingsClicked() {
        for(listener in listeners){
            listener.onBackendSettingsClicked()
        }
    }

    private fun onToggleCrowdMapEnabled() {
        for(listener in listeners){
            listener.onToggleCrowdMapEnabled()
        }
    }

    private fun onToggleMapsEnabled(){
        for(listener in listeners){
            listener.onToggleMapsEnabled()
        }
    }

    private fun onClearSDCardClicked() {
        for(listener in listeners){
            listener.onClearSDCardClicked()
        }
    }

    private fun onMyAccountClicked() {
        for(listener in listeners){
            listener.onMyAccountClicked()
        }
    }
}
