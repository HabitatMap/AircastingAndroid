package io.lunarlogic.aircasting.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.dashboard.SessionsTab
import io.lunarlogic.aircasting.screens.session_view.map.MapViewFixedMvcImpl
import io.lunarlogic.aircasting.screens.session_view.map.MapViewMobileActiveMvcImpl
import io.lunarlogic.aircasting.screens.session_view.map.MapViewMobileDormantMvcImpl


class GraphViewMvcImplFactory {
    companion object {
        open fun get(
            inflater: LayoutInflater,
            parent: ViewGroup?,
            supportFragmentManager: FragmentManager?,
            sessionTab: SessionsTab
        ): GraphViewMvcImpl {
            return when(sessionTab){
                SessionsTab.FOLLOWING -> GraphViewFollowingMvcImpl(inflater, parent, supportFragmentManager)
                SessionsTab.FIXED -> GraphViewFixedMvcImpl(inflater, parent, supportFragmentManager)
                SessionsTab.MOBILE_DORMANT -> GraphViewMobileDormantMvcImpl(inflater, parent, supportFragmentManager)
                SessionsTab.MOBILE_ACTIVE -> GraphViewMobileActiveMvcImpl(inflater, parent, supportFragmentManager)
            }
        }
    }
}
