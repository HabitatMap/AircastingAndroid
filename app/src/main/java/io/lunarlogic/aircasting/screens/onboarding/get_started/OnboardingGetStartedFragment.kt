package io.lunarlogic.aircasting.screens.onboarding.get_started

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.screens.common.BaseFragment
import io.lunarlogic.aircasting.screens.sync.synced.AirbeamSyncedController
import io.lunarlogic.aircasting.screens.sync.synced.AirbeamSyncedViewMvcImpl

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
