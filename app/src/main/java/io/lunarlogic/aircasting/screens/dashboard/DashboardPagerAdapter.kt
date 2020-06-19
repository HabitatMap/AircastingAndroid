package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import io.lunarlogic.aircasting.R

class DashboardPagerAdapter(private val mContext: Context, private val mFragmentManager: FragmentManager)
    : FragmentPagerAdapter(mFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> mContext.getString(R.string.dashboard_tabs_mobile_active)
            1 -> mContext.getString(R.string.dashboard_tabs_mobile_dormant)
            else -> ""
        }
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> MobileActiveFragment()
            1 -> MobileDormantFragment()
            else -> Fragment()
        }
    }
}