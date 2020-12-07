package io.lunarlogic.aircasting.screens.settings

import android.content.Context
import android.provider.Settings
import android.view.View
import io.lunarlogic.aircasting.screens.settings.myaccount.MyAccountActivity

class SettingsController(
    private val mContext: Context?,
    private val mViewMvc: SettingsViewMvc
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
        //TODO("Not yet implemented")
    }

    override fun onContributeCrowdMapSwitched() {
        //TODO("Not yet implemented")
    }

}