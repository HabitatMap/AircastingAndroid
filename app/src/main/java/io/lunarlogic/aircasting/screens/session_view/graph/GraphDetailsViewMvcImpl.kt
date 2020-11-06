package io.lunarlogic.aircasting.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvcImpl


class GraphDetailsViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?
): SessionDetailsViewMvcImpl(inflater, parent, supportFragmentManager) {
    override fun layoutId(): Int {
        return R.layout.activity_graph
    }
}
