package io.lunarlogic.aircasting.screens.new_session.select_device

class SelectDeviceTypeController(
    private var mViewMvc: SelectDeviceTypeViewMvc?
) {

    fun registerListener(listener: SelectDeviceTypeViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: SelectDeviceTypeViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }

    fun onDestroy() {
        mViewMvc = null
    }
}
