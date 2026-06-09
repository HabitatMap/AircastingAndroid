package pl.llp.aircasting.ui.view.screens.sync.confirmation

import pl.llp.aircasting.ui.view.common.BaseController

class AirbeamSyncConfirmationController(
    viewMvc: AirbeamSyncConfirmationViewMvcImpl?
) : BaseController<AirbeamSyncConfirmationViewMvcImpl>(viewMvc) {

    fun registerListener(listener: AirbeamSyncConfirmationViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: AirbeamSyncConfirmationViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }
}
