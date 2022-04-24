package pl.llp.aircasting.ui.view.screens.dashboard

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.dashboard.active.MobileActiveFragment
import pl.llp.aircasting.ui.view.screens.dashboard.dormant.MobileDormantFragment
import pl.llp.aircasting.ui.view.screens.dashboard.fixed.FixedFragment
import pl.llp.aircasting.ui.view.screens.dashboard.following.FollowingFragment

enum class SessionsTab(val value: Int) {
    FOLLOWING(0),
    MOBILE_ACTIVE(1),
    MOBILE_DORMANT(2),
    FIXED(3);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}

class DashboardPagerAdapter(
    private val mContext: Context,
    mFragmentManager: FragmentManager
) : FragmentPagerAdapter(mFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragments = hashMapOf(
        FOLLOWING_TAB_INDEX to FollowingFragment(),
        MOBILE_ACTIVE_TAB_INDEX to MobileActiveFragment(),
        MOBILE_DORMANT_TAB_INDEX to MobileDormantFragment(),
        FIXED_TAB_INDEX to FixedFragment()
    )

    companion object {
        val FOLLOWING_TAB_INDEX = SessionsTab.FOLLOWING.value
        val MOBILE_ACTIVE_TAB_INDEX = SessionsTab.MOBILE_ACTIVE.value
        val MOBILE_DORMANT_TAB_INDEX = SessionsTab.MOBILE_DORMANT.value
        val FIXED_TAB_INDEX = SessionsTab.FIXED.value

        const val TABS_COUNT = 4
    }

    override fun getCount(): Int {
        return TABS_COUNT
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            FOLLOWING_TAB_INDEX -> mContext.getString(R.string.dashboard_tabs_following)
            MOBILE_ACTIVE_TAB_INDEX -> mContext.getString(R.string.dashboard_tabs_mobile_active)
            MOBILE_DORMANT_TAB_INDEX -> mContext.getString(R.string.dashboard_tabs_mobile_dormant)
            FIXED_TAB_INDEX -> mContext.getString(R.string.dashboard_tabs_fixed)
            else -> ""
        }
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position] ?: Fragment()
    }
}
