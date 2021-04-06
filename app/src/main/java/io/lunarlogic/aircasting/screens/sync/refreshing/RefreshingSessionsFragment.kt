package io.lunarlogic.aircasting.screens.sync.refreshing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.screens.common.BaseFragment
import io.lunarlogic.aircasting.screens.onboarding.how_is_the_air.OnboardingHowsTheAirController
import io.lunarlogic.aircasting.screens.onboarding.how_is_the_air.OnboardingHowsTheAirViewMvcImpl

class RefreshingSessionsFragment(): BaseFragment<RefreshingSessionsViewMvcImpl, RefreshingSessionsController>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = RefreshingSessionsViewMvcImpl(layoutInflater, null)
        controller = RefreshingSessionsController()

        return view?.rootView
    }
}
