package pl.llp.aircasting.ui.view.fragments.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.screens.onboarding.get_started.OnboardingGetStartedController
import pl.llp.aircasting.ui.view.screens.onboarding.get_started.OnboardingGetStartedViewMvc
import pl.llp.aircasting.ui.view.screens.onboarding.get_started.OnboardingGetStartedViewMvcImpl

class OnboardingGetStartedFragment: BaseFragment<OnboardingGetStartedViewMvcImpl, OnboardingGetStartedController>() {
    lateinit var listener: OnboardingGetStartedViewMvc.Listener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = OnboardingGetStartedViewMvcImpl(layoutInflater, null)
        controller = OnboardingGetStartedController(view)

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
