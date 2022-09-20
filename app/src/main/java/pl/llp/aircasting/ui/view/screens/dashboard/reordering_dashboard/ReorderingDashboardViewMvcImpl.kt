package pl.llp.aircasting.ui.view.screens.dashboard.reordering_dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardViewMvcImpl

class ReorderingDashboardViewMvcImpl(
    mRootActivity: FragmentActivity,
    inflater: LayoutInflater,
    parent: ViewGroup?,
    fragmentManager: FragmentManager?,
    adapter: FragmentPagerAdapter,
    tabsCount: Int
) : DashboardViewMvcImpl(
    mRootActivity,
    inflater,
    parent,
    fragmentManager,
    adapter,
    tabsCount
) {
    override fun setupSwipeToRefreshLayout() {
        mSwipeRefreshLayout?.isEnabled = false
    }
}