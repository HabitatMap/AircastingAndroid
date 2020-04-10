package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.content.Context

class TurnOnAirBeamController(
    private val mContext: Context?,
    private val mViewMvc: TurnOnAirBeamViewMvc
) {

    fun registerListener(listener: TurnOnAirBeamViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: TurnOnAirBeamViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}