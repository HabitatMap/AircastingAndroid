package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context

class SelectDeviceTypeController(
    private val mContext: Context?,
    private val mViewMvc: SelectDeviceTypeViewMvc
) {

    fun registerListener(listener: SelectDeviceTypeViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: SelectDeviceTypeViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}