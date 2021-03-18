package io.lunarlogic.aircasting.screens.settings.clear_sd_card.sd_card_cleared

import io.lunarlogic.aircasting.screens.common.BaseController


class SDCardClearedController(
    private val mViewMvc: SDCardClearedViewMvc?
) : BaseController(mView = mViewMvc) {
    fun registerListener(listener: SDCardClearedViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: SDCardClearedViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }
}
