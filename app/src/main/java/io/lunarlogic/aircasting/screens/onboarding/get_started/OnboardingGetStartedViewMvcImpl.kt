package io.lunarlogic.aircasting.screens.onboarding.get_started

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class OnboardingGetStartedViewMvcImpl: BaseObservableViewMvc<OnboardingGetStartedViewMvc.Listener>, OnboardingGetStartedViewMvc {

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): super() {
        this.rootView = inflater.inflate(R.layout.onboarding_get_started, parent, false)

        val getStartedButton = rootView?.findViewById<Button>(R.id.get_started_button)
        getStartedButton?.setOnClickListener {
            onGetStartedClicked()
        }
    }

    private fun onGetStartedClicked() {
        for (listener in listeners) {
            listener.onGetStartedClicked()
        }
    }
}
