package io.lunarlogic.aircasting.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager

class GraphViewFixedMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?
): GraphViewMvcImpl(inflater, parent, supportFragmentManager) {

    override fun defaultZoomSpan(): Int {
        return 24 * 60 * 60 * 1000 // 24 hours
    }
}
