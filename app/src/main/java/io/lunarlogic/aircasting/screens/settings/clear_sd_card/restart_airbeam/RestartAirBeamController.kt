package io.lunarlogic.aircasting.screens.settings.clear_sd_card.restart_airbeam

import io.lunarlogic.aircasting.screens.common.BaseController

class RestartAirBeamController(
    var viewMvc: RestartAirBeamViewMvcImpl?
) : BaseController<RestartAirBeamViewMvcImpl>(viewMvc) {

    fun registerListener(listener: RestartAirBeamViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: RestartAirBeamViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }
}
