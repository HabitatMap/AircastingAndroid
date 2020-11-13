package io.lunarlogic.aircasting.screens.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.sensor.Session


open class MapViewMvcImplFactory {
    companion object {
        open fun get(
            inflater: LayoutInflater,
            parent: ViewGroup?,
            supportFragmentManager: FragmentManager?,
            sessionType: Session.Type,
            sessionStatus: Session.Status
        ): MapViewMvcImpl {
            return when {
                sessionType == Session.Type.MOBILE && sessionStatus == Session.Status.FINISHED -> MapViewMobileDormantMvcImpl(inflater, parent, supportFragmentManager)
                sessionType == Session.Type.MOBILE && sessionStatus != Session.Status.FINISHED -> MapViewMobileActiveMvcImpl(inflater, parent, supportFragmentManager)
                sessionType == Session.Type.FIXED -> MapViewFixedMvcImpl(inflater, parent, supportFragmentManager)
                else -> MapViewFixedMvcImpl(inflater, parent, supportFragmentManager)
            }
        }
    }
}
