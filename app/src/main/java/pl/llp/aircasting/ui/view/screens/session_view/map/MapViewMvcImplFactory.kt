package pl.llp.aircasting.ui.view.screens.session_view.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab

object MapViewMvcImplFactory {
    fun get(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager,
        sessionTab: SessionsTab
    ): MapViewMvcImpl {
        return when (sessionTab) {
            SessionsTab.FOLLOWING -> MapViewFollowingMvcImpl(
                inflater,
                parent,
                supportFragmentManager
            )
            SessionsTab.FIXED -> MapViewFixedMvcImpl(inflater, parent, supportFragmentManager)
            SessionsTab.MOBILE_DORMANT -> MapViewMobileDormantMvcImpl(
                inflater,
                parent,
                supportFragmentManager
            )
            SessionsTab.MOBILE_ACTIVE -> MapViewMobileActiveMvcImpl(
                inflater,
                parent,
                supportFragmentManager
            )
        }
    }
}
