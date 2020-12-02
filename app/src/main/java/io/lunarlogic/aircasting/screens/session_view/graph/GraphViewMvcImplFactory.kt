package io.lunarlogic.aircasting.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.models.Session


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

            return when(sessionStatus) {
                Session.Status.FINISHED -> GraphViewMobileDormantMvcImpl(inflater, parent, supportFragmentManager)
                Session.Status.RECORDING -> GraphViewMobileActiveMvcImpl(inflater, parent, supportFragmentManager)
                else -> GraphViewFixedMvcImpl(inflater, parent, supportFragmentManager)
            }
        }
    }
}
