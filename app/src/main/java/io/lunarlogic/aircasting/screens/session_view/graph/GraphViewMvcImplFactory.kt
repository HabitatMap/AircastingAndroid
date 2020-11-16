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
            sessionType: Session.Type
        ): GraphViewMvcImpl {
            return when(sessionType) {
                Session.Type.MOBILE -> GraphViewMobileMvcImpl(inflater, parent, supportFragmentManager)
                Session.Type.FIXED -> GraphViewFixedMvcImpl(inflater, parent, supportFragmentManager)
                else -> GraphViewMobileMvcImpl(inflater, parent, supportFragmentManager)
            }
        }
    }
}
