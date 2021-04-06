package io.lunarlogic.aircasting.screens.onboarding.your_privacy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.screens.common.BaseFragment
import io.lunarlogic.aircasting.screens.onboarding.how_is_the_air.OnboardingHowsTheAirController
import io.lunarlogic.aircasting.screens.onboarding.how_is_the_air.OnboardingHowsTheAirViewMvcImpl
import io.lunarlogic.aircasting.screens.onboarding.measure_and_map.OnboardingMeasureAndMapViewMvcImpl

class OnboardingYourPrivacyFragment: BaseFragment<OnboardingYourPrivacyViewMvcImpl, OnboardingYourPrivacyController>() {
    lateinit var listener: OnboardingYourPrivacyViewMvc.Listener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = OnboardingYourPrivacyViewMvcImpl(layoutInflater, null)
        controller = OnboardingYourPrivacyController(view)

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
