package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.content.Context

class TurnAirBeamController(
    private val mContext: Context?,
    private val mViewMvc: TurnAirBeamViewMvc
) {

    fun registerListener(listener: TurnAirBeamViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: TurnAirBeamViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}