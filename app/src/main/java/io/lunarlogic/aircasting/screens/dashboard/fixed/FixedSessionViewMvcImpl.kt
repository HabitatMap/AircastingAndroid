package io.lunarlogic.aircasting.screens.dashboard.fixed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.dashboard.SessionActionsBottomSheet
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvcImpl
import kotlinx.android.synthetic.main.session_card.view.*

class FixedSessionViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup,
    supportFragmentManager: FragmentManager
):
    SessionViewMvcImpl<FixedSessionViewMvc.Listener>(inflater, parent, supportFragmentManager),
    FixedSessionViewMvc,
    SessionActionsBottomSheet.Listener
{
    private val mMeasurementsDescriptionTextView: TextView?

    init {
        mMeasurementsDescriptionTextView = this.rootView?.session_measurements_description
        setMeasurementDescriptionText()
    }

    override fun showMeasurementsTableValues(): Boolean {
        return false
    }

    override fun showExpandedMeasurementsTableValues() = false

    override fun buildBottomSheet(): BottomSheet {
        return SessionActionsBottomSheet(this)
    }

    override fun showChart() = false

    override fun bindFollowButtons(sessionPresenter: SessionPresenter) {
        if (sessionPresenter.session?.followed == true) {
            mFollowButton.visibility = View.GONE
        } else {
            mUnfollowButton.visibility = View.GONE
        }
    }

    override fun deleteSessionPressed() {
        for (listener in listeners) {
            listener.onSessionDeleteClicked(mSessionPresenter!!.session!!)
        }
        dismissBottomSheet()
    }

    private fun setMeasurementDescriptionText() {
        mMeasurementsDescriptionTextView?.text = context.getString(R.string.parameters)
    }
}
