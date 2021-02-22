package io.lunarlogic.aircasting.screens.onboarding.measure_and_map

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class OnboardingMeasureAndMapViewMvcImpl: BaseObservableViewMvc<OnboardingMeasureAndMapViewMvc.Listener>, OnboardingMeasureAndMapViewMvc {
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
