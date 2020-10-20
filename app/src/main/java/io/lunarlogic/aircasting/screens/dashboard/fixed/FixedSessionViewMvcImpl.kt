package io.lunarlogic.aircasting.screens.dashboard.fixed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.dashboard.SessionActionsBottomSheet
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

    override fun deleteSessionPressed() {
        for (listener in listeners) {
            listener.onSessionDeleteClicked(mSessionPresenter!!.session!!)
        }
        dismissBottomSheet()
    }

    override fun chartUnitLabelId(): Int {
       return R.string.fixed_session_units_label
    }

}
