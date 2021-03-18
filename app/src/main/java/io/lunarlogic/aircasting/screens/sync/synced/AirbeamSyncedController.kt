package io.lunarlogic.aircasting.screens.sync.synced

class AirbeamSyncedController(
    private var mViewMvc: AirbeamSyncedViewMvc?
) {
    fun registerListener(listener: AirbeamSyncedViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: AirbeamSyncedViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }

    fun onDestroy() {
        mViewMvc = null
    }
}
