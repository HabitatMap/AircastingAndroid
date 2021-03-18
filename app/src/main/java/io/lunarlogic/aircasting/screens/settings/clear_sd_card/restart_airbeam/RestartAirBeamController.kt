package io.lunarlogic.aircasting.screens.settings.clear_sd_card.restart_airbeam

import android.content.Context
import io.lunarlogic.aircasting.screens.common.BaseController

class RestartAirBeamController(
    private var mViewMvc: RestartAirBeamViewMvc?
) : BaseController(mView = mViewMvc) {

    fun registerListener(listener: RestartAirBeamViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: RestartAirBeamViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }
}
