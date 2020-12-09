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
) : SettingsViewMvc.Listener {

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
        BackendSettingsDialog(fragmentManager, mSettings).show()
    }

    override fun onToggleCrowdMapEnabled() {
        mSettings.toggleCrowdMapEnabled()
    }

}
