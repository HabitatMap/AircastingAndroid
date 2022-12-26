package pl.llp.aircasting.ui.view.screens.dashboard.following

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.dashboard.fixed.FixedSessionViewMvcImpl
import pl.llp.aircasting.util.extensions.gone
import pl.llp.aircasting.util.extensions.visible

open class FollowingSessionViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup,
    supportFragmentManager: FragmentManager
) : FixedSessionViewMvcImpl<FollowingSessionViewMvc.Listener>(inflater, parent, supportFragmentManager),
    FollowingSessionViewMvc {

    private val noMeasurementsIcon: ImageView?
    private val noMeasurementsLabels: View?

    init {
        noMeasurementsIcon = this.rootView?.findViewById(R.id.session_no_measurements_icon)
        noMeasurementsLabels = this.rootView?.findViewById(R.id.session_no_measurements_labels)
    }

    override fun showMeasurementsTableValues(): Boolean {
        return true
    }

    override fun showChart() = true

    override fun bindFollowButtons() {
        mUnfollowButton.visible()
        mFollowButton.gone()
    }

    override fun bindMeasurementsTable() {
        mMeasurementsTableContainer.bindSession(mSessionPresenter, this::onMeasurementStreamChanged)
        if (mSessionPresenter?.session?.hasMeasurements() == true) {
            hideNoMeasurementsInfo()
        } else {
            showNoMeasurementsInfo()
        }
    }

    override fun bindCollapsedMeasurementsDescription() {
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
