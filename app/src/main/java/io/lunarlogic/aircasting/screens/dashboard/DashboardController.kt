package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import io.lunarlogic.aircasting.screens.selectdevice.SelectDeviceActivity

class DashboardController(
    private val mContext: Context?,
    private val mViewMvc: DashboardViewMvc
) : DashboardViewMvc.Listener {

    fun onStart() {
        mViewMvc.registerListener(this)
    }

    fun onStop() {
        mViewMvc.unregisterListener(this)
    }

    override fun onRecordNewSessionClicked() {
        SelectDeviceActivity.start(mContext)
    }

}