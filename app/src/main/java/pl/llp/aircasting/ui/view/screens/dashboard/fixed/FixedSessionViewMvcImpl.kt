package pl.llp.aircasting.ui.view.screens.dashboard.fixed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.dashboard.SessionViewMvcImpl

open class FixedSessionViewMvcImpl<ListenerType : FixedSessionViewMvc.Listener>(
    inflater: LayoutInflater,
    parent: ViewGroup,
    private val supportFragmentManager: FragmentManager
) : SessionViewMvcImpl<FixedSessionViewMvc.Listener>(inflater, parent, supportFragmentManager),
    FixedSessionViewMvc {

    override fun showMeasurementsTableValues(): Boolean {
        return false
    }

    override fun showExpandedMeasurementsTableValues() = true

    override fun bindCollapsedMeasurementsDescription() {
        mMeasurementsDescription?.text = context.getString(R.string.parameters)
    }

    override fun bindExpandedMeasurementsDescription() {
        mMeasurementsDescription?.text =
            context.getString(R.string.session_last_min_measurements_description)
    }

    override fun bindFollowButtons() {
        if (mSessionPresenter?.session?.followed == true) {
            mFollowButton.visibility = View.GONE
            mUnfollowButton.visibility = View.VISIBLE
        } else {
            mFollowButton.visibility = View.VISIBLE
            mUnfollowButton.visibility = View.GONE
        }
    }
}
