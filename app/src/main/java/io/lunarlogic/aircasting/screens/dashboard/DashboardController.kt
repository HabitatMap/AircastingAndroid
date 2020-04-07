package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context

class DashboardController(
    private val mContext: Context?,
    private var mViewMvc: DashboardViewMvcImpl
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