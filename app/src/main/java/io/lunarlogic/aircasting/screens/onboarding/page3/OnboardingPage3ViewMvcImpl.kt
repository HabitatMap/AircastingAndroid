package io.lunarlogic.aircasting.screens.onboarding.page3

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.onboarding.page3.OnboardingPage3ViewMvc

class OnboardingPage3ViewMvcImpl: BaseObservableViewMvc<OnboardingPage3ViewMvc.Listener>, OnboardingPage3ViewMvc {
    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): super() {
        this.rootView = inflater.inflate(R.layout.onboarding_page_3, parent, false)

        val continueButton = rootView?.findViewById<Button>(R.id.continue_button)
        continueButton?.setOnClickListener {
            onContinueClicked()
        }

        val learnMoreButton = rootView?.findViewById<Button>(R.id.learn_more_button)
        learnMoreButton?.setOnClickListener {
            onLearnMoreClicked()
        }
    }

    private fun onContinueClicked() {
        for (listener in listeners) {
            listener.onContinuePage3Clicked()
        }
    }

    private fun onLearnMoreClicked() {
        for (listener in listeners) {
            listener.onLearnMorePage3Clicked()
        }
    }
}
