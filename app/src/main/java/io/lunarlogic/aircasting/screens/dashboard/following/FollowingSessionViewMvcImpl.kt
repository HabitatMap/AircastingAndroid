package io.lunarlogic.aircasting.screens.dashboard.following

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.dashboard.ActiveSessionViewMvcImpl
import io.lunarlogic.aircasting.sensor.Session

class FollowingSessionViewMvcImpl:
    ActiveSessionViewMvcImpl<FollowingSessionViewMvc.Listener>,
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

    override fun layoutId(): Int {
        return R.layout.following_session
    }

    override fun buildBottomSheet(): BottomSheet? {
        return null;
    }

    override fun bindSession(session: Session) {
        bindSessionDetails(session)

        if (session.measurementsCount() > 0) {
            hideNoMeasurementsInfo()
            resetMeasurementsView()
            bindMeasurements(session)
            stretchTableLayout(session)
        }
        else {
            showNoMeasurementsInfo()
        }
    }

    private fun showNoMeasurementsInfo() {
        measurementsDescription?.visibility = View.GONE

        noMeasurementsIcon?.visibility = View.VISIBLE
        noMeasurementsLabels?.visibility = View.VISIBLE

        mExpandSessionButton.visibility = View.GONE
        mCollapseSessionButton.visibility = View.GONE
    }

    private fun hideNoMeasurementsInfo() {
        measurementsDescription?.visibility = View.VISIBLE

        noMeasurementsIcon?.visibility = View.GONE
        noMeasurementsLabels?.visibility = View.GONE

        mExpandSessionButton.visibility = View.VISIBLE
        mCollapseSessionButton.visibility = View.VISIBLE
    }

    override fun onMapButtonClicked() {
        mSession?.let {
            for (listener in listeners) {
                listener.onMapButtonClicked(it, mSelectedStream!!)
            }
        }
    }
}
