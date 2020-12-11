package io.lunarlogic.aircasting.screens.dashboard.active

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
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

    override fun bindCollapsedMeasurementsDesctription() {
        mMeasurementsDescription?.text = context.getString(R.string.session_last_sec_measurements_description)
    }

    override fun bindExpandedMeasurementsDesctription() {
        mMeasurementsDescription?.text = context.getString(R.string.session_last_sec_measurements_description)
    }

    override fun showExpandedMeasurementsTableValues() = true

    override fun buildBottomSheet(): BottomSheet? {
        return ActiveSessionActionsBottomSheet(this)
    }

    override fun disconnectSessionPressed() {
        for (listener in listeners) {
            listener.onSessionDisconnectClicked(mSessionPresenter!!.session!!)
        }
        dismissBottomSheet()
    }

    override fun reconnectSessionPressed() {
        for (listener in listeners) {
            listener.onSessionReconnectClicked(mSessionPresenter!!.session!!)
        }
        dismissBottomSheet()
    }

    override fun stopSessionPressed() {
        for (listener in listeners) {
            listener.onSessionStopClicked(mSessionPresenter!!.session!!)
        }
        dismissBottomSheet()
    }
}
