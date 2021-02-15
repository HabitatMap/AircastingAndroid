package io.lunarlogic.aircasting.screens.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc


class OnboardingViewMvcImpl: BaseObservableViewMvc<OnboardingViewMvc.Listener>, OnboardingViewMvc {

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): super() {
        this.rootView = inflater.inflate(R.layout.onboarding_page_1, parent, false)

        val getStartedButton = rootView?.findViewById<Button>(R.id.get_started_button)
        getStartedButton?.setOnClickListener {
            onGetStartedClicked()
        }
    }

    private fun onGetStartedClicked() {
        TODO("Not yet implemented")
    }
}
