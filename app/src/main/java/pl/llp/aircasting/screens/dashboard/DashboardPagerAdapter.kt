package pl.llp.aircasting.screens.dashboard

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import pl.llp.aircasting.R
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.dashboard.active.MobileActiveFragment
import pl.llp.aircasting.screens.dashboard.dormant.MobileDormantFragment
import pl.llp.aircasting.screens.dashboard.fixed.FixedFragment
import pl.llp.aircasting.screens.dashboard.following.FollowingFragment

enum class SessionsTab(val value: Int){
    FOLLOWING(0),
    MOBILE_ACTIVE(1),
    MOBILE_DORMANT(2),
    FIXED(3);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}

class DashboardPagerAdapter(private val mContext: Context, private val mFragmentManager: FragmentManager)
    : FragmentPagerAdapter(mFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    val fragments = hashMapOf(
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

        val TABS_COUNT = 4

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
        return TABS_COUNT
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
        return fragments[position] ?: Fragment()
    }
}
