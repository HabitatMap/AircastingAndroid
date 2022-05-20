package pl.llp.aircasting.ui.view.fragments.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.screens.onboarding.your_privacy.OnboardingYourPrivacyController
import pl.llp.aircasting.ui.view.screens.onboarding.your_privacy.OnboardingYourPrivacyViewMvc
import pl.llp.aircasting.ui.view.screens.onboarding.your_privacy.OnboardingYourPrivacyViewMvcImpl

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
