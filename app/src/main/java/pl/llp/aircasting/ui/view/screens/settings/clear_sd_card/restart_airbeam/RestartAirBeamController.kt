package pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.restart_airbeam

import pl.llp.aircasting.ui.view.screens.common.BaseController

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
