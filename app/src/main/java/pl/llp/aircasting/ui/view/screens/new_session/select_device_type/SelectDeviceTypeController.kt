package pl.llp.aircasting.ui.view.screens.new_session.select_device_type

import pl.llp.aircasting.ui.view.screens.common.BaseController

class SelectDeviceTypeController(
    var viewMvc: SelectDeviceTypeViewMvcImpl?
) : BaseController<SelectDeviceTypeViewMvcImpl>(viewMvc) {

    fun registerListener(listener: SelectDeviceTypeViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: SelectDeviceTypeViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }
}
