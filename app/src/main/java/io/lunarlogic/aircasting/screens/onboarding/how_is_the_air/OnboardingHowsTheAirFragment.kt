package io.lunarlogic.aircasting.screens.onboarding.how_is_the_air

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.screens.common.BaseFragment
import io.lunarlogic.aircasting.screens.onboarding.get_started.OnboardingGetStartedController
import io.lunarlogic.aircasting.screens.onboarding.get_started.OnboardingGetStartedViewMvcImpl

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
