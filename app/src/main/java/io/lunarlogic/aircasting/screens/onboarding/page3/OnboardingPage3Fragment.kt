package io.lunarlogic.aircasting.screens.onboarding.page3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class OnboardingPage3Fragment: Fragment() {
    private var controller: OnboardingPage3Controller? = null
    lateinit var listener: OnboardingPage3ViewMvc.Listener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = OnboardingPage3ViewMvcImpl(layoutInflater, null)
        controller = OnboardingPage3Controller(view)

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
