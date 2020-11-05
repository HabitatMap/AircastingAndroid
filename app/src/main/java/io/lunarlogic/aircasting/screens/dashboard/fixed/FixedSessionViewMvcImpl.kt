package io.lunarlogic.aircasting.screens.dashboard.fixed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.dashboard.SessionActionsBottomSheet
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvcImpl

class FixedSessionViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup,
    supportFragmentManager: FragmentManager
):
    SessionViewMvcImpl<FixedSessionViewMvc.Listener>(inflater, parent, supportFragmentManager),
    FixedSessionViewMvc,
    SessionActionsBottomSheet.Listener
{
    override fun showMeasurementsTableValues(): Boolean {
        return false
    }

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
}
