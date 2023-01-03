package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.dashboard.SessionViewMvcImpl
import pl.llp.aircasting.util.extensions.gone
import pl.llp.aircasting.util.extensions.visible

class MobileActiveSessionViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup,
    supportFragmentManager: FragmentManager
) : SessionViewMvcImpl<MobileActiveSessionViewMvc.Listener>(
    inflater,
    parent,
    supportFragmentManager
), MobileActiveSessionViewMvc {

    private val mDisconnectedView: DisconnectedView =
        DisconnectedView(context, this.rootView, supportFragmentManager)

    override fun showMeasurementsTableValues(): Boolean {
        return true
    }

    override fun showExpandedMeasurementsTableValues() = true

    override fun bindCollapsedMeasurementsDescription() {
        mMeasurementsDescription?.text =
            context.getString(R.string.session_last_sec_measurements_description)
    }

    override fun bindExpandedMeasurementsDescription() {
        bindCollapsedMeasurementsDescription()
    }

    override fun showChart() = true

    override fun bindExpanded() {
        if (mSessionPresenter?.isDisconnected() == true) {
            mDisconnectedView.show(mSessionPresenter)

            mSessionCardLayout.background =
                context.let { AppCompatResources.getDrawable(it, R.drawable.top_border) }
            mCollapseSessionButton.gone()
            mActionsButton.gone()
            mExpandSessionButton.gone()
            mExpandedSessionView.gone()
            mMeasurementsDescription?.gone()
            hideLoader()
        } else {
            mMeasurementsDescription?.visible()
            mActionsButton.visible()
            setExpandCollapseButton()
            mSessionCardLayout.background = null
            super.bindExpanded()

            mDisconnectedView.hide()
        }
    }
}
