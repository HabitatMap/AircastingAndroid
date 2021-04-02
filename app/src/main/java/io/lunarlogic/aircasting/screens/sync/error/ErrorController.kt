package io.lunarlogic.aircasting.screens.sync.error

import io.lunarlogic.aircasting.screens.common.BaseController

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
