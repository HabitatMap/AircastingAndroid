package io.lunarlogic.aircasting.screens.onboarding.page2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class OnboardingPage2Fragment: Fragment() {
    private var controller: OnboardingPage2Controller? = null
    lateinit var listener: OnboardingPage2ViewMvc.Listener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = OnboardingPage2ViewMvcImpl(layoutInflater, null)
        controller = OnboardingPage2Controller(view)

        return view.rootView
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
