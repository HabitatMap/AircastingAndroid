package pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.clearing_sd_card

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseViewMvc
import pl.llp.aircasting.util.startAnimation

class ClearingSDCardViewMvcImpl(inflater: LayoutInflater, parent: ViewGroup?) : BaseViewMvc(),
    ClearingSDCardViewMvc {

    init {
        this.rootView = inflater.inflate(R.layout.fragment_sd_card_clearing, parent, false)
        startLoader()
    }

    private fun startLoader() {
        val loader = rootView?.findViewById<ImageView>(R.id.loader)
        loader?.startAnimation()
    }
}
