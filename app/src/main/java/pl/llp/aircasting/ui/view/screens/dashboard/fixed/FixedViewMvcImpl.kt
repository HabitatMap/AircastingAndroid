package pl.llp.aircasting.ui.view.screens.dashboard.fixed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsRecyclerAdapter
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvcImpl

class FixedViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager
): SessionsViewMvcImpl<FixedSessionViewMvc.Listener>(inflater, parent, supportFragmentManager),
    FixedSessionViewMvc.Listener {

    override fun buildAdapter(
        inflater: LayoutInflater,
        supportFragmentManager: FragmentManager
    ): SessionsRecyclerAdapter<FixedSessionViewMvc.Listener> {
        return FixedRecyclerAdapter(
            mRecyclerSessions,
            inflater,
            this,
            supportFragmentManager
        )
    }

    override fun onSessionEditClicked(session: Session) {
        for (listener in listeners) {
            listener.onEditSessionClicked(session)
        }
    }

    override fun onSessionShareClicked(session: Session) {
        for (listener in listeners) {
            listener.onShareSessionClicked(session)
        }
    }

    override fun onSessionDeleteClicked(session: Session) {
        for (listener in listeners) {
            listener.onDeleteSessionClicked(session)
        }
    }

    override fun layoutId(): Int {
        return R.id.empty_dashboard
    }

    override fun showDidYouKnowBox(): Boolean {
        return false
    }

    override fun recordNewSessionButtonId(): Int {
        return R.id.dashboard_record_new_session_button
    }

    override fun onExploreNewSessionsButtonID(): Int {
        return R.id.txtExploreExistingSessions
    }

    override fun addTouchHelperToRecyclerView() {
        // Do nothing here
    }

}
