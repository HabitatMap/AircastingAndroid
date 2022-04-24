package pl.llp.aircasting.ui.view.screens.settings.clear_sd_card

import android.view.LayoutInflater
import android.view.ViewGroup
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.common.BaseViewMvc

class ClearSDCardViewMvcImpl: BaseViewMvc, ClearSDCardViewMvc {

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): super() {
        this.rootView = inflater.inflate(R.layout.activity_clear_sd_card, parent, false)
    }
}
