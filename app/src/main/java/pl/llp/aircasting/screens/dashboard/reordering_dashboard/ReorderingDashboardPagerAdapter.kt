package pl.llp.aircasting.screens.dashboard.reordering_dashboard

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.dashboard.DashboardPagerAdapter
import pl.llp.aircasting.screens.dashboard.following.FollowingFragment
import pl.llp.aircasting.screens.dashboard.reordering_following.ReorderingFollowingFragment
<<<<<<< HEAD
=======

enum class SessionsTab(val value: Int){
    FOLLOWING(0);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}
>>>>>>> 6c94fa1c (first operational version)

class ReorderingDashboardPagerAdapter(private val mContext: Context, private val mFragmentManager: FragmentManager)
    : FragmentPagerAdapter(mFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

<<<<<<< HEAD
    val fragment = ReorderingFollowingFragment()
=======
    val fragment = hashMapOf(
        DashboardPagerAdapter.FOLLOWING_TAB_INDEX to ReorderingFollowingFragment()
    )
>>>>>>> 921b2413 (rebase 3)

    companion object {
        val TABS_COUNT = 1
    }

    override fun getCount(): Int {
        return TABS_COUNT
    }

    override fun getPageTitle(position: Int): CharSequence {
        return "Following"
    }

    override fun getItem(position: Int): Fragment {
        return fragment
    }

}
