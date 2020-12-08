package io.lunarlogic.aircasting.screens.settings

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import androidx.fragment.app.FragmentManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class SettingsViewMvcImpl : BaseObservableViewMvc<SettingsViewMvc.Listener>, SettingsViewMvc  {

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?,
        fragmentManager: FragmentManager?
    ) : super(){
        this.rootView = inflater.inflate(R.layout.fragment_settings, parent, false)

        val myAccountButton = rootView?.findViewById<Button>(R.id.myAccount_Button)
        myAccountButton?.setOnClickListener {
            onMyaccountClicked()
        }

        val contributeToCrowdMapSwitch = rootView?.findViewById<SwitchMaterial>(R.id.crowd_map_settings_switch)
        contributeToCrowdMapSwitch?.setOnCheckedChangeListener { p0, p1 ->
            Log.i("SETTINGS_FRAGMENT", "Crowd Map Switch switched")
            onContributeCrowdMapSwitched()
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

    private fun onContributeCrowdMapSwitched() {
        for(listener in listeners){
            listener.onContributeCrowdMapSwitched()
        }
    }

    private fun onMyaccountClicked() {
        for(listener in listeners){
            listener.onMyAccountClicked()
        }
    }


}