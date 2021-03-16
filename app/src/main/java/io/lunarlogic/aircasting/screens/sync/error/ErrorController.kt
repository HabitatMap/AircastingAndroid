package io.lunarlogic.aircasting.screens.sync.error

class ErrorController(
    private val mViewMvc: ErrorViewMvc
) {
    fun registerListener(listener: ErrorViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: ErrorViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}
