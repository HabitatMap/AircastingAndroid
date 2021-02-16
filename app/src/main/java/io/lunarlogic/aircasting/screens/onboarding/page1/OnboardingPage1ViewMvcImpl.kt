package io.lunarlogic.aircasting.screens.onboarding.page1

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class OnboardingPage1ViewMvcImpl: BaseObservableViewMvc<OnboardingPage1ViewMvc.Listener>, OnboardingPage1ViewMvc {

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
        for (listener in listeners) {
            listener.onGetStartedClicked()
        }
    }
}
