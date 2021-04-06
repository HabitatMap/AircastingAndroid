package io.lunarlogic.aircasting.screens.onboarding.measure_and_map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.screens.common.BaseFragment
import io.lunarlogic.aircasting.screens.onboarding.how_is_the_air.OnboardingHowsTheAirController
import io.lunarlogic.aircasting.screens.onboarding.how_is_the_air.OnboardingHowsTheAirViewMvcImpl

class OnboardingMeasureAndMapFragment: BaseFragment<OnboardingMeasureAndMapViewMvcImpl, OnboardingMeasureAndMapController>() {
    lateinit var listener: OnboardingMeasureAndMapViewMvc.Listener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = OnboardingMeasureAndMapViewMvcImpl(layoutInflater, null)
        controller = OnboardingMeasureAndMapController(view)

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
