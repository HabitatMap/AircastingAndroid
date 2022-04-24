package pl.llp.aircasting.ui.view.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.prominent_app_bar.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.adjustMenuVisibility
import pl.llp.aircasting.ui.view.screens.common.BaseFragment
import javax.inject.Inject

class DashboardFragment : BaseFragment<DashboardViewMvcImpl, DashboardController>() {

    @Inject
    lateinit var settings: Settings
    private var mSearchIcon: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        val pagerAdapter = context?.let { DashboardPagerAdapter(it, childFragmentManager) }
        view = pagerAdapter?.let {
            DashboardViewMvcImpl(
                inflater, container, childFragmentManager,
                it, DashboardPagerAdapter.TABS_COUNT
            )
        }
        controller = DashboardController(view)
        val tabId = arguments?.get("tabId") as Int?
        controller?.onCreate(tabId)

        mSearchIcon = activity?.search_follow_icon

        return view?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupReorder()
    }

    private fun setupReorder() {
        tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                isAdjustable(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                isAdjustable(tab.position)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                isAdjustable(tab.position)
            }
        })
    }

    private fun isAdjustable(position: Int) {
        activity?.let {
            if (position == 0) {
                adjustMenuVisibility(it, true, settings.getFollowedSessionsNumber())
                mSearchIcon?.visibility = View.VISIBLE
            } else {
                adjustMenuVisibility(it, false)
                mSearchIcon?.visibility = View.INVISIBLE
            }
        }
    }

}
