package pl.llp.aircasting.ui.view.fragments.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.screens.onboarding.how_is_the_air.OnboardingHowsTheAirController
import pl.llp.aircasting.ui.view.screens.onboarding.how_is_the_air.OnboardingHowsTheAirViewMvc
import pl.llp.aircasting.ui.view.screens.onboarding.how_is_the_air.OnboardingHowsTheAirViewMvcImpl

class OnboardingHowsTheAirFragment:  BaseFragment<OnboardingHowsTheAirViewMvcImpl, OnboardingHowsTheAirController>() {
    lateinit var listener: OnboardingHowsTheAirViewMvc.Listener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = OnboardingHowsTheAirViewMvcImpl(layoutInflater, null)
        controller = OnboardingHowsTheAirController(view)

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
