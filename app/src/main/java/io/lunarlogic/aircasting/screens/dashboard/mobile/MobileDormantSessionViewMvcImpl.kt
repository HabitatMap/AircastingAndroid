package io.lunarlogic.aircasting.screens.dashboard.mobile

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.dashboard.SessionActionsBottomSheet
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvcImpl
import kotlinx.android.synthetic.main.session_card.view.*

class MobileDormantSessionViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup,
    supporFragmentManager: FragmentManager
):
    SessionViewMvcImpl<MobileDormantSessionViewMvc.Listener>(inflater, parent, supporFragmentManager),
    MobileDormantSessionViewMvc,
    SessionActionsBottomSheet.Listener
{
    override fun showMeasurementsTableValues(): Boolean {
        return false
    }

    override fun showExpandedMeasurementsTableValues() = true

    override fun bindCollapsedMeasurementsDesctription() {
        mMeasurementsDescription?.text = context.getString(R.string.parameters)
    }

    override fun bindExpandedMeasurementsDesctription() {
        mMeasurementsDescription?.text = context.getString(R.string.session_avg_measurements_description)
    }

    override fun buildBottomSheet(): BottomSheet {
        return SessionActionsBottomSheet(this)
    }

    override fun showChart() = false

    override fun deleteSessionPressed() {
        for (listener in listeners) {
            listener.onSessionDeleteClicked(mSessionPresenter!!.session!!)
        }
        dismissBottomSheet()
    }
}
