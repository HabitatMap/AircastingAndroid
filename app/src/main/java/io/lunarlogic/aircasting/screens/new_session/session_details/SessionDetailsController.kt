package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context

class SessionDetailsController(
    private val mContext: Context?,
    private val mViewMvc: SessionDetailsViewMvc
) {

    fun registerListener(listener: SessionDetailsViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: SessionDetailsViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}