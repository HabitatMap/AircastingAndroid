package io.lunarlogic.aircasting.screens.new_session.select_device

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