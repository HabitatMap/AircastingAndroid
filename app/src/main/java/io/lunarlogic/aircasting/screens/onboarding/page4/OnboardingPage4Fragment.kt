package io.lunarlogic.aircasting.screens.onboarding.page4

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class OnboardingPage4Fragment: Fragment() {
    private var controller: OnboardingPage4Controller? = null
    lateinit var listener: OnboardingPage4ViewMvc.Listener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = OnboardingPage4ViewMvcImpl(layoutInflater, null)
        controller = OnboardingPage4Controller(view)

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
