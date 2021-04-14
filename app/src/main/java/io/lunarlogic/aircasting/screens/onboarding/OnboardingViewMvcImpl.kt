package io.lunarlogic.aircasting.screens.onboarding

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
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

    override fun changeProgressBarColorToGreen() {
        this.rootView?.progress_bar?.progressDrawable?.setColorFilter(context.resources.getColor(R.color.aircasting_green), PorterDuff.Mode.SRC_IN)
    }

    override fun changeProgressBarColorToBlue() {
        this.rootView?.progress_bar?.progressDrawable?.setColorFilter(context.resources.getColor(R.color.aircasting_blue_400), PorterDuff.Mode.SRC_IN)
    }

    override fun hideProgressBar() {
        this.rootView?.progress_bar_frame?.visibility = View.GONE
    }

    override fun showProgressBar() {

        this.rootView?.progress_bar_frame?.visibility = View.VISIBLE
    }
}
