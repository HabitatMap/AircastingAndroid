package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import pl.llp.aircasting.ui.view.common.BaseController

class AirBeamConnectedController(viewMvc: AirBeamConnectedViewMvcImpl?): BaseController<AirBeamConnectedViewMvcImpl>(viewMvc) {
    fun registerListener(listener: AirBeamConnectedViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: AirBeamConnectedViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }
}
