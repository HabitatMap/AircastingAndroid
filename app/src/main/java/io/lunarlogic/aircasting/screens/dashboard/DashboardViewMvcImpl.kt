package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.Session

class DashboardViewMvcImpl: BaseObservableViewMvc<DashboardViewMvc.Listener>, DashboardViewMvc {
    constructor(
        inflater: LayoutInflater, parent: ViewGroup?,
        fragmentManager: FragmentManager?
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_dashboard, parent, false)
        val pager = rootView?.findViewById<ViewPager>(R.id.pager)
        fragmentManager?.let { pager?.adapter = DashboardPagerAdapter(context, it) }
    }
}