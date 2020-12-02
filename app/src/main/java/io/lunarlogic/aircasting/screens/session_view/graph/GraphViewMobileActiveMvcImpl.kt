package io.lunarlogic.aircasting.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager

class GraphViewMobileActiveMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?
    ): GraphViewMvcImpl(inflater, parent, supportFragmentManager) {

        override fun defaultZoomSpan(): Int {
            return 10 * 60 * 1000 // 10 minutes
        }
    }
