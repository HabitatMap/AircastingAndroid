package pl.llp.aircasting.screens.settings.clear_sd_card.sd_card_cleared

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.common.BaseObservableViewMvc

class SDCardClearedViewMvcImpl: BaseObservableViewMvc<SDCardClearedViewMvc.Listener>, SDCardClearedViewMvc {

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_sd_card_cleared, parent, false)

        val button = rootView?.findViewById<Button>(R.id.sd_card_cleared_continue_button)
        button?.setOnClickListener {
            onSDCardClearedContinueClicked()
        }
    }

    private fun onSDCardClearedContinueClicked() {
        for (listener in listeners) {
            listener.onSDCardClearedConfirmationClicked()
        }
    }
}
