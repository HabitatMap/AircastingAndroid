package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.content.Context

class AirBeamConnectedController(
    private val mContext: Context?,
    private val mViewMvc: AirBeamConnectedViewMvc
) {

    fun registerListener(listener: AirBeamConnectedViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: AirBeamConnectedViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}