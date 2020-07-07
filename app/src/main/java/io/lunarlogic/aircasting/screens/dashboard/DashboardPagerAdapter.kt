package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import io.lunarlogic.aircasting.R

class DashboardPagerAdapter(private val mContext: Context, private val mFragmentManager: FragmentManager)
    : FragmentPagerAdapter(mFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    companion object {
        val MOBILE_ACTIVE_TAB_INDEX = 0
        val MOBILE_DORMANT_TAB_INDEX = 1
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            MOBILE_ACTIVE_TAB_INDEX -> mContext.getString(R.string.dashboard_tabs_mobile_active)
            MOBILE_DORMANT_TAB_INDEX -> mContext.getString(R.string.dashboard_tabs_mobile_dormant)
            else -> ""
        }
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            MOBILE_ACTIVE_TAB_INDEX -> MobileActiveFragment()
            MOBILE_DORMANT_TAB_INDEX -> MobileDormantFragment()
            else -> Fragment()
        }
    }
}