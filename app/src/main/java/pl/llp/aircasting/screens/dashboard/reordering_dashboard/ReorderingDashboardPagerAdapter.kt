package pl.llp.aircasting.screens.dashboard.reordering_dashboard

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.dashboard.DashboardPagerAdapter
import pl.llp.aircasting.screens.dashboard.following.FollowingFragment
import pl.llp.aircasting.screens.dashboard.reordering_following.ReorderingFollowingFragment

enum class SessionsTab(val value: Int){
    FOLLOWING(0);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}

class ReorderingDashboardPagerAdapter(private val mContext: Context, private val mFragmentManager: FragmentManager)
    : FragmentPagerAdapter(mFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    val fragment = hashMapOf(
        DashboardPagerAdapter.FOLLOWING_TAB_INDEX to ReorderingFollowingFragment()
    )

    companion object {
        val FOLLOWING_TAB_INDEX = pl.llp.aircasting.screens.dashboard.SessionsTab.FOLLOWING.value

        val TABS_COUNT = 1
    }

    override fun getCount(): Int {
        return TABS_COUNT
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            FOLLOWING_TAB_INDEX -> "Following Reordering"
            else -> ""
        }
    }

    override fun getItem(position: Int): Fragment {
        return fragment[position] ?: Fragment()
    }

}
