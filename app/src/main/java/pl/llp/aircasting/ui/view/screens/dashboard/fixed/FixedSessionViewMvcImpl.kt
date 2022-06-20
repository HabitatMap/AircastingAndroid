package pl.llp.aircasting.ui.view.screens.dashboard.fixed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.SessionActionsBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.dashboard.SessionViewMvcImpl

class FixedSessionViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup,
    supportFragmentManager: FragmentManager
) :
    SessionViewMvcImpl<FixedSessionViewMvc.Listener>(inflater, parent, supportFragmentManager),
    FixedSessionViewMvc,
    SessionActionsBottomSheet.Listener {

    override fun showMeasurementsTableValues(): Boolean {
        return false
    }

    override fun bindExpanded(sessionPresenter: SessionPresenter) {
        if (sessionPresenter.isExternal()) mActionsButton.visibility = View.GONE
        super.bindExpanded(sessionPresenter)
    }

    override fun showExpandedMeasurementsTableValues() = true

    override fun bindCollapsedMeasurementsDescription() {
        mMeasurementsDescription?.text = context.getString(R.string.parameters)
    }

    override fun bindExpandedMeasurementsDescription() {
        mMeasurementsDescription?.text =
            context.getString(R.string.session_last_min_measurements_description)
    }

    override fun buildBottomSheet(sessionPresenter: SessionPresenter?): BottomSheet {
        return SessionActionsBottomSheet(this)
    }

    override fun showChart() = false

    override fun bindFollowButtons(sessionPresenter: SessionPresenter) {
        if (sessionPresenter.session?.followed == true) {
            mFollowButton.visibility = View.GONE
            mUnfollowButton.visibility = View.VISIBLE
        } else {
            mFollowButton.visibility = View.VISIBLE
            mUnfollowButton.visibility = View.GONE
        }
    }

    override fun editSessionPressed() {
        val session = mSessionPresenter?.session ?: return

        for (listener in listeners) {
            listener.onSessionEditClicked(session)
        }
        dismissBottomSheet()
    }

    override fun shareSessionPressed() {
        val session = mSessionPresenter?.session ?: return

        for (listener in listeners) {
            listener.onSessionShareClicked(session)
        }
        dismissBottomSheet()
    }

    override fun deleteSessionPressed() {
        val session = mSessionPresenter?.session ?: return

        for (listener in listeners) {
            listener.onSessionDeleteClicked(session)
        }
        dismissBottomSheet()
    }
}
