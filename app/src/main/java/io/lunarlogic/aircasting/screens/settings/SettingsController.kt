package io.lunarlogic.aircasting.screens.settings

import android.content.Context
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.settings.myaccount.MyAccountActivity

class SettingsController(
    private val mContext: Context?,
    private val mViewMvc: SettingsViewMvc,
    private val mSettings: Settings,
    private val fragmentManager: FragmentManager
) : SettingsViewMvc.Listener, SettingsViewMvc.BackendSettingsDialogListener {

    fun onStart(){
        mViewMvc.registerListener(this)
    }

    fun onStop(){
        mViewMvc.unregisterListener(this)
    }

    override fun onMyAccountClicked() {
        MyAccountActivity.start(mContext)
    }

    override fun onBackendSettingsClicked() {
        startBackendSettingsDialog()
    }

    override fun onToggleCrowdMapEnabled() {
        mSettings.toggleCrowdMapEnabled()
    }

    override fun confirmClicked(urlValue: String, portValue: String) {
        mSettings.backendSettingsChanged(urlValue, portValue)
    }

    fun startBackendSettingsDialog(){
        val url = mSettings.getBackendUrl()
        val port = mSettings.getBackendPort()
        BackendSettingsDialog(fragmentManager, url, port, this).show()
    }


}
