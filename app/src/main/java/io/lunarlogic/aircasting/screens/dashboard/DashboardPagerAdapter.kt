package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.dashboard.fixed.FixedFragment
import io.lunarlogic.aircasting.screens.dashboard.mobile.MobileActiveFragment
import io.lunarlogic.aircasting.screens.dashboard.mobile.MobileDormantFragment

class DashboardPagerAdapter(private val mContext: Context, private val mFragmentManager: FragmentManager)
    : FragmentPagerAdapter(mFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    companion object {
        val MOBILE_ACTIVE_TAB_INDEX = 0
        val MOBILE_DORMANT_TAB_INDEX = 1
        val FIXED = 2
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            MOBILE_ACTIVE_TAB_INDEX -> mContext.getString(R.string.dashboard_tabs_mobile_active)
            MOBILE_DORMANT_TAB_INDEX -> mContext.getString(R.string.dashboard_tabs_mobile_dormant)
            FIXED -> mContext.getString(R.string.dashboard_tabs_fixed)
            else -> ""
        }
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            MOBILE_ACTIVE_TAB_INDEX -> MobileActiveFragment()
            MOBILE_DORMANT_TAB_INDEX -> MobileDormantFragment()
            FIXED -> FixedFragment()
            else -> Fragment()
        }
    }
}