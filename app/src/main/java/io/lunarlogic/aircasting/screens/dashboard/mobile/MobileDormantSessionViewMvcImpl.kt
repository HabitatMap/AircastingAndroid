package io.lunarlogic.aircasting.screens.dashboard.mobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.dashboard.SessionActionsBottomSheet
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvcImpl

class MobileDormantSessionViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup,
    supporFragmentManager: FragmentManager
):
    SessionViewMvcImpl<MobileDormantSessionViewMvc.Listener>(inflater, parent, supporFragmentManager),
    MobileDormantSessionViewMvc,
    SessionActionsBottomSheet.Listener
{

    override fun layoutId(): Int {
        return R.layout.dormant_session
    }

    override fun showMeasurementsTableValues(): Boolean {
        return false
    }

    override fun buildBottomSheet(): BottomSheet {
        return SessionActionsBottomSheet(this)
    }

    override fun deleteSessionPressed() {
        for (listener in listeners) {
            listener.onSessionDeleteClicked(mSession!!)
        }
        dismissBottomSheet()
    }

    override fun onMapButtonClicked() {
        mSession?.let {
            for (listener in listeners) {
                listener.onMapButtonClicked(it, mSelectedStream)
            }
        }
    }
}
