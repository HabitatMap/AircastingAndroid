package io.lunarlogic.aircasting.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.session_view.map.MapViewFixedMvcImpl
import io.lunarlogic.aircasting.screens.session_view.map.MapViewMobileActiveMvcImpl
import io.lunarlogic.aircasting.screens.session_view.map.MapViewMobileDormantMvcImpl


class GraphViewMvcImplFactory {
    companion object {
        open fun get(
            inflater: LayoutInflater,
            parent: ViewGroup?,
            supportFragmentManager: FragmentManager?,
            sessionType: Session.Type,
            sessionStatus: Session.Status
        ): GraphViewMvcImpl {
            if (sessionType == Session.Type.FIXED) {
                return GraphViewFixedMvcImpl(inflater, parent, supportFragmentManager)
            }
//
            return when(sessionStatus) {
                Session.Status.FINISHED -> GraphViewMobileMvcImpl(inflater, parent, supportFragmentManager)
                Session.Status.RECORDING -> GraphViewMobileMvcImpl(inflater, parent, supportFragmentManager)
                else -> GraphViewFixedMvcImpl(inflater, parent, supportFragmentManager)
            } //TODO: differ between dormant and actove session
//            return when(sessionType) {
//                Session.Type.MOBILE -> GraphViewMobileMvcImpl(inflater, parent, supportFragmentManager)
//                Session.Type.FIXED -> GraphViewFixedMvcImpl(inflater, parent, supportFragmentManager)
//                else -> GraphViewMobileMvcImpl(inflater, parent, supportFragmentManager)
//            }
        }
    }
}
