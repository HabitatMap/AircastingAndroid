package pl.llp.aircasting.ui.view.screens.dashboard.following

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.SessionCardListener
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.dashboard.SessionViewMvcImpl
import pl.llp.aircasting.util.extensions.gone
import pl.llp.aircasting.util.extensions.visible

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

    override fun bindFollowButtons(sessionPresenter: SessionPresenter) {
        mUnfollowButton.visible()
        mFollowButton.gone()
    }

    override fun buildBottomSheet(sessionPresenter: SessionPresenter?): BottomSheet? {
        return null
    }

    override fun bindMeasurementsTable() {
        val session = mSessionPresenter?.session
        val hasMeasurements = session?.hasMeasurements()

        // show MeasurementTable when there are measurements and the noMeasurementView is visible
        if (session == null || hasMeasurements == true) {
            hideNoMeasurementsInfo()
            mMeasurementsTableContainer.bindSession(
                mSessionPresenter,
                this::onMeasurementStreamChanged
            )
        }
    }
    // TODO: We'll need to check to see what's happenning with the previously followed sessions which are being bound to the newest one here
    // It may be also related to the RecyclerView adapter card!

    private fun isNoMeasurementViewVisible(): Boolean {
        return noMeasurementsIcon?.isVisible == true && noMeasurementsLabels?.isVisible == true
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
