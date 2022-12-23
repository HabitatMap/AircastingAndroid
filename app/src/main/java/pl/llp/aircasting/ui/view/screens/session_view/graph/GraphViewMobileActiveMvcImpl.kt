package pl.llp.aircasting.ui.view.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.session_actions.mobile.active.MobileActiveSessionActionsBottomSheet

class GraphViewMobileActiveMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager
) : GraphViewMvcImpl(inflater, parent, supportFragmentManager),
    MobileActiveSessionActionsBottomSheet.Listener {

    override fun bindSessionMeasurementsDescription() {
        mSessionMeasurementsDescription?.text =
            context.getString(R.string.session_last_sec_measurements_description)
    }

    override fun defaultZoomSpan(): Int {
        return 30 * 60 * 1000 // 30 minutes
    }

    override fun addNotePressed() {
        val session = mSessionPresenter?.session ?: return
        for (listener in listeners) {
            listener.addNoteClicked(session)
        }
        dismissBottomSheet()
    }

    override fun onFinishSessionConfirmed(session: Session) {
        val session = mSessionPresenter?.session ?: return

        for (listener in listeners) {
            listener.onFinishSessionConfirmed(session)
        }
        dismissBottomSheet()
    }

    override fun disconnectSessionPressed() {
        val session = mSessionPresenter?.session ?: return
        for (listener in listeners) {
            listener.onSessionDisconnectClicked(session)
        }
        dismissBottomSheet()
    }

    override fun getSessionType(): Session.Type {
        return Session.Type.MOBILE
    }
}
