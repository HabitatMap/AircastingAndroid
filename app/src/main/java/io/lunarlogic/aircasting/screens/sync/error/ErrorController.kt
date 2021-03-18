package io.lunarlogic.aircasting.screens.sync.error

class ErrorController(
    private var mViewMvc: ErrorViewMvc?
) {
    fun registerListener(listener: ErrorViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: ErrorViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }

    fun onDestroy() {
        mViewMvc = null
    }
}
