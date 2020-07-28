package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.dashboard.fixed.FixedFragment
import io.lunarlogic.aircasting.screens.dashboard.following.FollowingFragment
import io.lunarlogic.aircasting.screens.dashboard.mobile.MobileActiveFragment
import io.lunarlogic.aircasting.screens.dashboard.mobile.MobileDormantFragment
import io.lunarlogic.aircasting.sensor.Session

class DashboardPagerAdapter(private val mContext: Context, private val mFragmentManager: FragmentManager)
    : FragmentPagerAdapter(mFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    companion object {
        val FOLLOWING_TAB_INDEX = 0
        val MOBILE_ACTIVE_TAB_INDEX = 1
        val MOBILE_DORMANT_TAB_INDEX = 2
        val FIXED_TAB_INDEX = 3

        fun tabIndexForSessionType(sessionType: Session.Type, sessionStatus: Session.Status): Int {
            if (sessionType == Session.Type.MOBILE && sessionStatus == Session.Status.RECORDING) {
                return MOBILE_ACTIVE_TAB_INDEX
            }

            if (sessionType == Session.Type.MOBILE && sessionStatus == Session.Status.FINISHED) {
                return MOBILE_DORMANT_TAB_INDEX
            }

            return FOLLOWING_TAB_INDEX
        }
    }

    override fun getCount(): Int {
        return 4
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            FOLLOWING_TAB_INDEX -> "Following"
            MOBILE_ACTIVE_TAB_INDEX -> mContext.getString(R.string.dashboard_tabs_mobile_active)
            MOBILE_DORMANT_TAB_INDEX -> mContext.getString(R.string.dashboard_tabs_mobile_dormant)
            FIXED_TAB_INDEX -> mContext.getString(R.string.dashboard_tabs_fixed)
            else -> ""
        }
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            FOLLOWING_TAB_INDEX -> FollowingFragment()
            MOBILE_ACTIVE_TAB_INDEX -> MobileActiveFragment()
            MOBILE_DORMANT_TAB_INDEX -> MobileDormantFragment()
            FIXED_TAB_INDEX -> FixedFragment()
            else -> Fragment()
        }
    }
}