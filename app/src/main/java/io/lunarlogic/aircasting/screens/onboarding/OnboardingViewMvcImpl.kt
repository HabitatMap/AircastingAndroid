package io.lunarlogic.aircasting.screens.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseViewMvc
import kotlinx.android.synthetic.main.activity_onboarding.view.*


class OnboardingViewMvcImpl: BaseViewMvc, OnboardingViewMvc {

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): super() {
        this.rootView = inflater.inflate(R.layout.activity_onboarding, parent, false)
    }
}
