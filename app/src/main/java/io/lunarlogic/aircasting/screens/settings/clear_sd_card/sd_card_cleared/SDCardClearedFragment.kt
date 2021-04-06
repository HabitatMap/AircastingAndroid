package io.lunarlogic.aircasting.screens.settings.clear_sd_card.sd_card_cleared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.screens.common.BaseFragment
import io.lunarlogic.aircasting.screens.onboarding.how_is_the_air.OnboardingHowsTheAirController
import io.lunarlogic.aircasting.screens.onboarding.how_is_the_air.OnboardingHowsTheAirViewMvcImpl

class SDCardClearedFragment: BaseFragment<SDCardClearedViewMvcImpl, SDCardClearedController>() {
    lateinit var listener: SDCardClearedViewMvc.Listener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

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
