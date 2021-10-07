package pl.llp.aircasting.screens.dashboard.reordering_dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.common.BaseViewMvc

class ReorderingDashboardViewMvcImpl: BaseViewMvc, ReorderingDashboardViewMvc {

    private val mPager: ViewPager?

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?,
        fragmentManager: FragmentManager?
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_dashboard, parent, false)
        mPager = rootView?.findViewById(R.id.pager)

        mPager?.offscreenPageLimit = ReorderingDashboardPagerAdapter.TABS_COUNT
        fragmentManager?.let { mPager?.adapter = ReorderingDashboardPagerAdapter(context, it) }

        setTabsMargins()
    }

    private fun setTabsMargins() {
        val tabs: TabLayout? = rootView?.findViewById(R.id.tabs)
        val firstTab = tabs?.getChildAt(0)
        val firstTabParams = firstTab?.layoutParams as ViewGroup.MarginLayoutParams
        val leftMargin = context.resources.getDimension(R.dimen.navigation_tabs_left_margin).toInt()
        val rightMargin = context.resources.getDimension(R.dimen.keyline_4).toInt()

        firstTabParams.setMargins(leftMargin, 0, rightMargin, 0)
        firstTab.requestLayout()
    }

    override fun goToTab(tabId: Int) {
        mPager?.currentItem = tabId
    }

}
