package pl.llp.aircasting.screens.settings.clear_sd_card.sd_card_cleared

import pl.llp.aircasting.screens.common.BaseController

class SDCardClearedController(
    viewMvc: SDCardClearedViewMvcImpl?
) : BaseController<SDCardClearedViewMvcImpl>(viewMvc) {
    fun registerListener(listener: SDCardClearedViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: SDCardClearedViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }
}
