package io.lunarlogic.aircasting.screens.onboarding.page4

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class OnboardingPage4ViewMvcImpl: BaseObservableViewMvc<OnboardingPage4ViewMvc.Listener>, OnboardingPage4ViewMvc {
    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): super() {
        this.rootView = inflater.inflate(R.layout.onboarding_page_4, parent, false)

        val acceptButton = rootView?.findViewById<Button>(R.id.accept_button)
        acceptButton?.setOnClickListener {
            onAcceptClicked()
        }

        val learnMoreButton = rootView?.findViewById<Button>(R.id.learn_more_button)
        learnMoreButton?.setOnClickListener {
            onLearnMoreClicked()
        }
    }

    private fun onAcceptClicked() {
        for (listener in listeners) {
            listener.onAcceptClicked()
        }
    }

    private fun onLearnMoreClicked() {
        for (listener in listeners) {
            listener.onLearnMorePage4Clicked()
        }
    }
}
