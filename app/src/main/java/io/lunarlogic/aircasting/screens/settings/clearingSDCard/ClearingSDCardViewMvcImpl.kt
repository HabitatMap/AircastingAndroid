package io.lunarlogic.aircasting.screens.settings.clearingSDCard

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.AnimatedLoader
import io.lunarlogic.aircasting.screens.common.BaseViewMvc

class ClearingSDCardViewMvcImpl: BaseViewMvc, ClearingSDCardViewMvc {

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?
        ): super() {
            this.rootView = inflater.inflate(R.layout.fragment_sd_card_clearing, parent, false)

            startLoader()
        }

    private fun startLoader() {
        val loader = rootView?.findViewById<ImageView>(R.id.loader)
        AnimatedLoader(loader).start()
    }
}
