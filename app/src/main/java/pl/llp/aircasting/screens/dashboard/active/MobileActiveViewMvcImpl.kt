package pl.llp.aircasting.screens.dashboard.active

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.dashboard.SessionsRecyclerAdapter
import pl.llp.aircasting.screens.dashboard.SessionsViewMvcImpl

class MobileActiveViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager
): SessionsViewMvcImpl<MobileActiveSessionViewMvc.Listener>(inflater, parent, supportFragmentManager),
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

    override fun onSessionDisconnectClicked(session: Session) {
        for (listener in listeners) {
            listener.onDisconnectSessionClicked(session)
        }
    }

    override fun addNoteClicked(session: Session) {
        for (listener in listeners) {
            listener.addNoteClicked(session)
        }
    }

    override fun onSessionReconnectClicked(session: Session) {
        for (listener in listeners) {
            session.deviceId?.let { deviceId ->
                listener.onReconnectSessionClicked(session)
            }
        }
    }

    override fun onFinishSessionConfirmed(session: Session) {
        for (listener in listeners) {
            listener.onFinishSessionConfirmed(session)
        }
    }

    override fun onFinishAndSyncSessionConfirmed(session: Session) {
        for (listener in listeners) {
            listener.onFinishAndSyncSessionConfirmed(session)
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

}
