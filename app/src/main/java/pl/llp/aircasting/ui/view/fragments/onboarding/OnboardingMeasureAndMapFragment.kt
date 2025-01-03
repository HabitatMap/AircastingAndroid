package pl.llp.aircasting.ui.view.fragments.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.screens.onboarding.measure_and_map.OnboardingMeasureAndMapController
import pl.llp.aircasting.ui.view.screens.onboarding.measure_and_map.OnboardingMeasureAndMapViewMvc
import pl.llp.aircasting.ui.view.screens.onboarding.measure_and_map.OnboardingMeasureAndMapViewMvcImpl

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
        listener.let { controller?.registerListener(it) }
    }

    override fun onStop() {
        super.onStop()
        controller?.unregisterListener(listener)
    }
}
