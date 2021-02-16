package io.lunarlogic.aircasting.screens.onboarding.page2

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.onboarding.page2.OnboardingPage2ViewMvc

class OnboardingPage2ViewMvcImpl: BaseObservableViewMvc<OnboardingPage2ViewMvc.Listener>, OnboardingPage2ViewMvc {

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): super() {
        this.rootView = inflater.inflate(R.layout.onboarding_page_2, parent, false)

        val continuButton = rootView?.findViewById<Button>(R.id.continue_button)
        continuButton?.setOnClickListener {
            onContinueButtonClicked()
        }
    }

    private fun onContinueButtonClicked() {
        for (listener in listeners) {
            listener.onContinuePage2Clicked()
        }
    }
}
