package pl.llp.aircasting.ui.view.screens.dashboard.fixed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsRecyclerAdapter
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvcImpl

open class FixedViewMvcImpl<ListenerType : FixedSessionViewMvc.Listener>(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    val supportFragmentManager: FragmentManager,
) : SessionsViewMvcImpl<FixedSessionViewMvc.Listener>(
    inflater,
    parent,
    supportFragmentManager,
),
    FixedSessionViewMvc.Listener {

    override fun buildAdapter(
        inflater: LayoutInflater,
        supportFragmentManager: FragmentManager
    ): SessionsRecyclerAdapter<FixedSessionViewMvc.Listener> {
        return FixedRecyclerAdapter<FixedSessionViewMvc.Listener>(
            mRecyclerSessions,
            inflater,
            this,
            supportFragmentManager,
        )
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
}
