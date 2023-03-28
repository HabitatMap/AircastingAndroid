package pl.llp.aircasting.ui.view.screens.dashboard.following

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsRecyclerAdapter
import pl.llp.aircasting.ui.view.screens.dashboard.fixed.FixedSessionViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.fixed.FixedViewMvcImpl

open class FollowingViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager,
    reloadSessionCallback: suspend (uuid: String) -> SessionWithStreamsAndMeasurementsDBObject?
) : FixedViewMvcImpl<FollowingSessionViewMvc.Listener>(
    inflater,
    parent,
    supportFragmentManager,
    reloadSessionCallback
),
    FollowingSessionViewMvc.Listener {

    override fun buildAdapter(
        inflater: LayoutInflater,
        supportFragmentManager: FragmentManager
    ): SessionsRecyclerAdapter<FixedSessionViewMvc.Listener> {
        return FollowingRecyclerAdapter(
            mRecyclerSessions,
            inflater,
            this,
            supportFragmentManager,
            reloadSessionCallback
        )
    }
}
