package io.lunarlogic.aircasting.screens.onboarding.get_started

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class OnboardingGetStartedFragment: Fragment() {
    private var controller: OnboardingGetStartedController? = null
    lateinit var listener: OnboardingGetStartedViewMvc.Listener
    private var view: OnboardingGetStartedViewMvcImpl? = null

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

    override fun onDestroyView() {
        super.onDestroyView()
        controller?.onDestroy()
        controller = null
        view = null
    }
    override fun onDestroy() {
        super.onDestroy()
        controller?.onDestroy()
        controller = null
        view = null
    }
}
