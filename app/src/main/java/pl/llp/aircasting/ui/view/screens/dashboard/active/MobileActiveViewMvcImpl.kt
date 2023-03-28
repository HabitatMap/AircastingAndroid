package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsRecyclerAdapter
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvcImpl

class MobileActiveViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager,
) : SessionsViewMvcImpl<MobileActiveSessionViewMvc.Listener>(
    inflater,
    parent,
    supportFragmentManager,
), MobileActiveSessionViewMvc.Listener {

    override fun buildAdapter(
        inflater: LayoutInflater,
        supportFragmentManager: FragmentManager
    ): SessionsRecyclerAdapter<MobileActiveSessionViewMvc.Listener> {
        return MobileActiveRecyclerAdapter(
            mRecyclerSessions,
            inflater,
            this,
            supportFragmentManager,
            reloadSession
        )
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
}
