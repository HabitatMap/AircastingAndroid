package pl.llp.aircasting.screens.settings.clear_sd_card.clearing_sd_card

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import pl.llp.aircasting.R
import pl.llp.aircasting.lib.AnimatedLoader
import pl.llp.aircasting.screens.common.BaseViewMvc

class ClearingSDCardViewMvcImpl(inflater: LayoutInflater, parent: ViewGroup?) : BaseViewMvc(),
    ClearingSDCardViewMvc {

    init {
        this.rootView = inflater.inflate(R.layout.fragment_sd_card_clearing, parent, false)
        startLoader()
    }

    private fun startLoader() {
        val loader = rootView?.findViewById<ImageView>(R.id.loader)
        AnimatedLoader(loader).start()
    }
}
