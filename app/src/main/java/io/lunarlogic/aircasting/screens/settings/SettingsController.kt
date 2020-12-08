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
        // todo:  moving to dialog, analogicznie do tego co mi Ania podesłała
        BackendSettingsDialog(fragmentManager, mSettings).show()
        // todo: mSettings.backendSettingsChanged() od wartości odtrzymanych z dialogu <- to w dialogu sie dzieje chyba
    }

    override fun onContributeCrowdMapSwitched() {
        mSettings.crowdMapSettingSwitched()
    }

}