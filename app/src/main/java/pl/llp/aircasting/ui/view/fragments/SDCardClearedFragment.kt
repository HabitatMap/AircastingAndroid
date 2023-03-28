package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.sd_card_cleared.SDCardClearedController
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.sd_card_cleared.SDCardClearedViewMvc
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.sd_card_cleared.SDCardClearedViewMvcImpl

class SDCardClearedFragment: BaseFragment<SDCardClearedViewMvcImpl, SDCardClearedController>() {
    lateinit var listener: SDCardClearedViewMvc.Listener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .userDependentComponent.inject(this)

        view = SDCardClearedViewMvcImpl(layoutInflater, null)
        controller = SDCardClearedController(view)

        return view?.rootView
    }

    override fun onStart() {
        super.onStart()
        controller?.registerListener(listener)
    }

    override fun onStop() {
        super.onStop()
        controller?.unregisterListener(listener)
    }
}
