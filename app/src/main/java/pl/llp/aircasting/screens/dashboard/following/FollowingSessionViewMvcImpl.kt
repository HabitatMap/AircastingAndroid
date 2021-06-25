package pl.llp.aircasting.screens.dashboard.following

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.common.BottomSheet
import pl.llp.aircasting.screens.dashboard.SessionCardListener
import pl.llp.aircasting.screens.dashboard.SessionPresenter
import pl.llp.aircasting.screens.dashboard.SessionViewMvcImpl

class FollowingSessionViewMvcImpl:
    SessionViewMvcImpl<SessionCardListener>,
    FollowingSessionViewMvc {

    val noMeasurementsIcon: ImageView?
    val noMeasurementsLabels: View?

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup,
        supportFragmentManager: FragmentManager
    ): super(inflater, parent, supportFragmentManager) {
        val actionsView = this.rootView?.findViewById<ImageView>(R.id.session_actions_button)
        actionsView?.visibility = View.GONE

        noMeasurementsIcon = this.rootView?.findViewById(R.id.session_no_measurements_icon)
        noMeasurementsLabels = this.rootView?.findViewById(R.id.session_no_measurements_labels)
    }

    override fun showMeasurementsTableValues(): Boolean {
        return true
    }

    override fun showExpandedMeasurementsTableValues() = true
      
    override fun bindFollowButtons(sessionPresenter: SessionPresenter) {
        mUnfollowButton.visibility = View.VISIBLE
        mFollowButton.visibility = View.GONE
    }

    override fun buildBottomSheet(sessionPresenter: SessionPresenter?): BottomSheet? {
        return null;
    }

    override fun bindMeasurementsTable() {
        val session = mSessionPresenter?.session
        if (session == null || session.measurementsCount() > 0) {
            hideNoMeasurementsInfo()
            mMeasurementsTableContainer.bindSession(mSessionPresenter, this::onMeasurementStreamChanged)
        } else {
            showNoMeasurementsInfo()
        }
    }

    override fun bindCollapsedMeasurementsDesctription() {
        mMeasurementsDescription?.text = context.getString(R.string.session_last_min_measurements_description)
    }

    override fun bindExpandedMeasurementsDesctription() {
        mMeasurementsDescription?.text = context.getString(R.string.session_last_min_measurements_description)
    }

    private fun showNoMeasurementsInfo() {
        mMeasurementsDescription?.visibility = View.GONE
        noMeasurementsIcon?.visibility = View.VISIBLE
        noMeasurementsLabels?.visibility = View.VISIBLE
        setExpandCollapseButton()
    }

    private fun hideNoMeasurementsInfo() {
        mMeasurementsDescription?.visibility = View.VISIBLE
        noMeasurementsIcon?.visibility = View.GONE
        noMeasurementsLabels?.visibility = View.GONE
        setExpandCollapseButton()
    }
}
