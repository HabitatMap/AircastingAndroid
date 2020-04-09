package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity

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
        NewSessionActivity.start(mContext)
    }
}