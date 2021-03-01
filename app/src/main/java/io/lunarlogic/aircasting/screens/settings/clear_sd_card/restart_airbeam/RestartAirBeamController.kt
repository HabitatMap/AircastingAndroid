package io.lunarlogic.aircasting.screens.settings.clear_sd_card.restart_airbeam

import android.content.Context

class RestartAirBeamController(
    private val mContext: Context?,
    private val mViewMvc: RestartAirBeamViewMvc
) {

    fun registerListener(listener: RestartAirBeamViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: RestartAirBeamViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}
