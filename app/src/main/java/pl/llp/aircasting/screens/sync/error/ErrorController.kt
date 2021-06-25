package pl.llp.aircasting.screens.sync.error

import pl.llp.aircasting.screens.common.BaseController

class ErrorController(
    viewMvc: ErrorViewMvcImpl?
): BaseController<ErrorViewMvcImpl>(viewMvc) {
    fun registerListener(listener: ErrorViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: ErrorViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }
}
