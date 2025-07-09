package pl.llp.aircasting.ui.view.screens.onboarding

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_onboarding.view.progress_bar
import kotlinx.android.synthetic.main.activity_onboarding.view.progress_bar_frame
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseViewMvc
import pl.llp.aircasting.ui.view.common.ViewEdge
import pl.llp.aircasting.ui.view.common.updateInsets


class OnboardingViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseViewMvc(), OnboardingViewMvc {

    init {
        this.rootView = inflater.inflate(R.layout.activity_onboarding, parent, false)
    }

    override fun changeProgressBarColorToGreen() {
        this.rootView?.progress_bar?.progressDrawable?.setColorFilter(
            context.resources.getColor(R.color.aircasting_green),
            PorterDuff.Mode.SRC_IN
        )
    }

    override fun changeProgressBarColorToBlue() {
        this.rootView?.progress_bar?.progressDrawable?.setColorFilter(
            context.resources.getColor(R.color.aircasting_blue_400),
            PorterDuff.Mode.SRC_IN
        )
    }

    override fun hideProgressBar() {
        this.rootView?.progress_bar_frame?.visibility = View.GONE
    }

    override fun showProgressBar() {
        this.rootView?.progress_bar_frame?.visibility = View.VISIBLE
        this.rootView?.progress_bar_frame?.updateInsets(ViewEdge.TOP)
    }
}
