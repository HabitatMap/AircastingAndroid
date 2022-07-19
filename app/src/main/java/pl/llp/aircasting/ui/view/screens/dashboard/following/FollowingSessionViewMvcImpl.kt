package pl.llp.aircasting.ui.view.screens.dashboard.following

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.SessionCardListener
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.dashboard.SessionViewMvcImpl
import pl.llp.aircasting.util.gone
import pl.llp.aircasting.util.visible

open class FollowingSessionViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup,
    supportFragmentManager: FragmentManager
) :
    SessionViewMvcImpl<SessionCardListener>(inflater, parent, supportFragmentManager),
    FollowingSessionViewMvc {

    private val noMeasurementsIcon: ImageView?
    private val noMeasurementsLabels: View?

    init {
        val actionsView = this.rootView?.findViewById<ImageView>(R.id.session_actions_button)
        actionsView?.gone()
        noMeasurementsIcon = this.rootView?.findViewById(R.id.session_no_measurements_icon)
        noMeasurementsLabels = this.rootView?.findViewById(R.id.session_no_measurements_labels)
    }

    override fun showMeasurementsTableValues(): Boolean {
        return true
    }

    override fun showExpandedMeasurementsTableValues() = true

    override fun bindFollowButtons() {
        mUnfollowButton.visible()
        mFollowButton.gone()
    }

    override fun buildBottomSheet(sessionPresenter: SessionPresenter?): BottomSheet? {
        return null
    }

    override fun bindMeasurementsTable() {
        val session = mSessionPresenter?.session
        if (session == null || session.hasMeasurements()) {
            hideNoMeasurementsInfo()
            mMeasurementsTableContainer.bindSession(mSessionPresenter, this::onMeasurementStreamChanged)
        } else {
            showNoMeasurementsInfo()
        }
    }

    override fun bindCollapsedMeasurementsDescription() {
        mMeasurementsDescription?.text =
            context.getString(R.string.session_last_min_measurements_description)
    }

    override fun bindExpandedMeasurementsDescription() {
        mMeasurementsDescription?.text =
            context.getString(R.string.session_last_min_measurements_description)
    }

    private fun hideNoMeasurementsInfo() {
        mMeasurementsDescription?.visible()

        noMeasurementsIcon?.gone()
        noMeasurementsLabels?.gone()
    }

    private fun showNoMeasurementsInfo() {
        mMeasurementsDescription?.gone()

        noMeasurementsIcon?.visible()
        noMeasurementsLabels?.visible()
    }
}
