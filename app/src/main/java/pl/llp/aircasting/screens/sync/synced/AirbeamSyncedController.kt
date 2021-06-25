package pl.llp.aircasting.screens.sync.synced

import pl.llp.aircasting.screens.common.BaseController

class AirbeamSyncedController(
        viewMvc: AirbeamSyncedViewMvcImpl?
    ): BaseController<AirbeamSyncedViewMvcImpl>(viewMvc) {
    fun registerListener(listener: AirbeamSyncedViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: AirbeamSyncedViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }
}
