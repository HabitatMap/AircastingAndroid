package pl.llp.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.common.BaseViewMvc

class DashboardViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    fragmentManager: FragmentManager?,
    adapter: FragmentPagerAdapter,
    tabsCount: Int
) : BaseViewMvc(), DashboardViewMvc {
    private val mPager: ViewPager?
    private var tabs: TabLayout? = null

    init {
        this.rootView = inflater.inflate(R.layout.fragment_dashboard, parent, false)
        tabs = rootView?.findViewById(R.id.tabs)
        mPager = rootView?.findViewById(R.id.pager)
        mPager?.offscreenPageLimit = tabsCount
        fragmentManager?.let { mPager?.adapter = adapter }
        setTabsMargins()
    }

    override fun goToTab(tabId: Int) {
        mPager?.currentItem = tabId
    }

    private fun setTabsMargins() {
        val firstTab = tabs?.getChildAt(0)
        val firstTabParams = firstTab?.layoutParams as ViewGroup.MarginLayoutParams
        val leftMargin = context.resources.getDimension(R.dimen.navigation_tabs_left_margin).toInt()
        val rightMargin = context.resources.getDimension(R.dimen.keyline_4).toInt()

        firstTabParams.setMargins(leftMargin, 0, rightMargin, 0)
        firstTab.requestLayout()
    }

}
