package io.lunarlogic.aircasting.screens.dashboard.mobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.dashboard.ActiveSessionActionsBottomSheet
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvcImpl

class MobileActiveSessionViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup,
    supportFragmentManager: FragmentManager
):
    SessionViewMvcImpl<MobileActiveSessionViewMvc.Listener>(inflater, parent, supportFragmentManager),
    MobileActiveSessionViewMvc,
    ActiveSessionActionsBottomSheet.Listener
{
    override fun showMeasurementsTableValues(): Boolean {
        return true
    }

    override fun buildBottomSheet(): BottomSheet? {
        return ActiveSessionActionsBottomSheet(this)
    }

    override fun stopSessionPressed() {
        for (listener in listeners) {
            listener.onSessionStopClicked(mSession!!)
        }
        dismissBottomSheet()
    }
}
