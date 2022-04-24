package pl.llp.aircasting.ui.view.screens.onboarding.get_started

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.common.BaseObservableViewMvc

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
