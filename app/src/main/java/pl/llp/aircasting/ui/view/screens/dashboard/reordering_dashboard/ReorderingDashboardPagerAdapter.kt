package pl.llp.aircasting.ui.view.screens.dashboard.reordering_dashboard

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import pl.llp.aircasting.ui.view.screens.dashboard.reordering_following.ReorderingFollowingFragment


class ReorderingDashboardPagerAdapter(private val mContext: Context, private val mFragmentManager: FragmentManager)
    : FragmentPagerAdapter(mFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    val fragment = ReorderingFollowingFragment()

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
