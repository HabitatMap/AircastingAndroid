package pl.llp.aircasting.ui.view.screens.sync.error

import pl.llp.aircasting.ui.view.common.BaseController

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
