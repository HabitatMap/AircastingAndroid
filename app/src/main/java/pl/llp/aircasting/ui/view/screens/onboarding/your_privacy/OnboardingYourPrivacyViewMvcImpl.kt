package pl.llp.aircasting.ui.view.screens.onboarding.your_privacy

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc

class OnboardingYourPrivacyViewMvcImpl: BaseObservableViewMvc<OnboardingYourPrivacyViewMvc.Listener>, OnboardingYourPrivacyViewMvc {
    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): super() {
        this.rootView = inflater.inflate(R.layout.onboarding_your_privacy, parent, false)

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
            listener.onLearnMoreYourPrivacyClicked()
        }
    }
}
