package io.lunarlogic.aircasting.screens.settings

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import com.google.android.material.switchmaterial.SwitchMaterial
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import kotlinx.android.synthetic.main.fragment_settings.view.*

class SettingsViewMvcImpl : BaseObservableViewMvc<SettingsViewMvc.Listener>, SettingsViewMvc  {

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?,
        fragmentManager: FragmentManager?
    ) : super(){
        this.rootView = inflater.inflate(R.layout.fragment_settings, parent, false)

        val myAccountButton = rootView?.myAccount_Button
        myAccountButton?.setOnClickListener {
            onMyAccountClicked()
        }

        val contributeToCrowdMapSwitch = rootView?.crowd_map_settings_switch
        contributeToCrowdMapSwitch?.setOnCheckedChangeListener { p0, p1 ->
            Log.i("SETTINGS_FRAGMENT", "Crowd Map Switch switched")
            onToggleCrowdMapEnabled()
        }

        val backendSettingsButton = rootView?.findViewById<Button>(R.id.backend_settings_button)
        backendSettingsButton?.setOnClickListener {
            onBackendSettingsClicked()
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

    private fun onMyAccountClicked() {
        for(listener in listeners){
            listener.onMyAccountClicked()
        }
    }
}
