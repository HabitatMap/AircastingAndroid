package io.lunarlogic.aircasting.screens.sync.synced

import io.lunarlogic.aircasting.screens.common.BaseController

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
