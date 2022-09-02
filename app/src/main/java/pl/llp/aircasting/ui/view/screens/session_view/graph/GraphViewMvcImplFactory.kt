package pl.llp.aircasting.ui.view.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab

class GraphViewMvcImplFactory {
    companion object {
        fun get(
            inflater: LayoutInflater,
            parent: ViewGroup?,
            supportFragmentManager: FragmentManager?,
            sessionTab: SessionsTab
        ): GraphViewMvcImpl {
            return when (sessionTab) {
                SessionsTab.FOLLOWING -> GraphViewFollowingMvcImpl(
                    inflater,
                    parent,
                    supportFragmentManager
                )
                SessionsTab.FIXED -> GraphViewFixedMvcImpl(inflater, parent, supportFragmentManager)
                SessionsTab.MOBILE_DORMANT -> GraphViewMobileDormantMvcImpl(
                    inflater,
                    parent,
                    supportFragmentManager
                )
                SessionsTab.MOBILE_ACTIVE -> GraphViewMobileActiveMvcImpl(
                    inflater,
                    parent,
                    supportFragmentManager
                )
            }
        }
    }
}
