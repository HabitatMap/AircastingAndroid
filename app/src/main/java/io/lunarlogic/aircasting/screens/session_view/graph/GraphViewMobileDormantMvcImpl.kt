package io.lunarlogic.aircasting.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager

class GraphViewMobileDormantMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?
): GraphViewMobileMvcImpl(inflater, parent, supportFragmentManager) {

    override fun bindStatisticsContainer() {
        mStatisticsContainer = null
    }
}
