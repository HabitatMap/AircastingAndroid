package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.content.Context

class TurnOffLocationServicesController(
    private val mContext: Context?,
    private val mViewMvc: TurnOffLocationServicesViewMvc
) {
    fun registerListener(listener: TurnOffLocationServicesViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: TurnOffLocationServicesViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}
