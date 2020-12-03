package io.lunarlogic.aircasting.screens.session_view.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.dashboard.SessionsTab


open class MapViewMvcImplFactory {
    companion object {
        open fun get(
            inflater: LayoutInflater,
            parent: ViewGroup?,
            supportFragmentManager: FragmentManager?,
            sessionTab: SessionsTab
        ): MapViewMvcImpl {
            return when(sessionTab){
                SessionsTab.FOLLOWING -> MapViewFixedMvcImpl(inflater, parent, supportFragmentManager)
                SessionsTab.FIXED -> MapViewFixedMvcImpl(inflater, parent, supportFragmentManager)
                SessionsTab.MOBILE_DORMANT -> MapViewMobileDormantMvcImpl(inflater, parent, supportFragmentManager)
                SessionsTab.MOBILE_ACTIVE -> MapViewMobileActiveMvcImpl(inflater, parent, supportFragmentManager)
            }
        }
    }
}
