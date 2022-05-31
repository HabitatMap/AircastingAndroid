package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsRecyclerAdapter
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvcImpl

class MobileActiveViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager
) : SessionsViewMvcImpl<MobileActiveSessionViewMvc.Listener>(
    inflater,
    parent,
    supportFragmentManager
),
    MobileActiveSessionViewMvc.Listener {

    override fun buildAdapter(
        inflater: LayoutInflater,
        supportFragmentManager: FragmentManager
    ): SessionsRecyclerAdapter<MobileActiveSessionViewMvc.Listener> {
        return MobileActiveRecyclerAdapter(
            inflater,
            this,
            supportFragmentManager
        )
    }

    override fun onSessionDisconnectClicked(localSession: LocalSession) {
        for (listener in listeners) {
            listener.onDisconnectSessionClicked(localSession)
        }
    }

    override fun addNoteClicked(localSession: LocalSession) {
        for (listener in listeners) {
            listener.addNoteClicked(localSession)
        }
    }

    override fun onSessionReconnectClicked(localSession: LocalSession) {
        for (listener in listeners) {
            localSession.deviceId?.let { deviceId ->
                listener.onReconnectSessionClicked(localSession)
            }
        }
    }

    override fun onFinishSessionConfirmed(localSession: LocalSession) {
        for (listener in listeners) {
            listener.onFinishSessionConfirmed(localSession)
        }
    }

    override fun onFinishAndSyncSessionConfirmed(localSession: LocalSession) {
        for (listener in listeners) {
            listener.onFinishAndSyncSessionConfirmed(localSession)
        }
    }

    override fun layoutId(): Int {
        return R.id.empty_mobile_dashboard
    }

    override fun showDidYouKnowBox(): Boolean {
        return true
    }

    override fun recordNewSessionButtonId(): Int {
        return R.id.dashboard_mobile_record_new_session_button
    }

    override fun onExploreNewSessionsButtonID(): Int {
        return R.id.txtExploreExistingSessions
    }

    override fun addTouchHelperToRecyclerView() {
        // Do nothing here
    }
}
