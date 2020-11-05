package io.lunarlogic.aircasting.screens.dashboard.following

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.dashboard.SessionCardListener
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvcImpl
import io.lunarlogic.aircasting.sensor.Session

class FollowingSessionViewMvcImpl:
    SessionViewMvcImpl<SessionCardListener>,
    FollowingSessionViewMvc {

    val noMeasurementsIcon: ImageView?
    val noMeasurementsLabels: View?
    val measurementsDescription: TextView?

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup,
        supportFragmentManager: FragmentManager
    ): super(inflater, parent, supportFragmentManager) {
        val actionsView = this.rootView?.findViewById<ImageView>(R.id.session_actions_button)
        actionsView?.visibility = View.GONE

        noMeasurementsIcon = this.rootView?.findViewById(R.id.session_no_measurements_icon)
        noMeasurementsLabels = this.rootView?.findViewById(R.id.session_no_measurements_labels)
        measurementsDescription = this.rootView?.findViewById(R.id.session_measurements_description)
    }

    override fun showMeasurementsTableValues(): Boolean {
        return true
    }

    override fun bindFollowButtons(sessionPresenter: SessionPresenter) {
        mUnfollowButton.visibility = View.VISIBLE
        mFollowButton.visibility = View.GONE
    }

    override fun buildBottomSheet(): BottomSheet? {
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

    private fun showNoMeasurementsInfo() {
        measurementsDescription?.visibility = View.GONE

        noMeasurementsIcon?.visibility = View.VISIBLE
        noMeasurementsLabels?.visibility = View.VISIBLE

        mExpandSessionButton.visibility = View.GONE
    }

    private fun hideNoMeasurementsInfo() {
        measurementsDescription?.visibility = View.VISIBLE

        noMeasurementsIcon?.visibility = View.GONE
        noMeasurementsLabels?.visibility = View.GONE

        mExpandSessionButton.visibility = View.VISIBLE
    }
}
