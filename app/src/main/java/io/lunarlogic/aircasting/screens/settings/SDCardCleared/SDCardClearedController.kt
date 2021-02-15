package io.lunarlogic.aircasting.screens.settings.SDCardCleared


class SDCardClearedController(
    private val mViewMvc: SDCardClearedViewMvc
) {
    fun registerListener(listener: SDCardClearedViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: SDCardClearedViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}
