package pl.llp.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.screens.common.BaseFragment
import pl.llp.aircasting.screens.dashboard.following.FollowingFragment
import javax.inject.Inject

class DashboardFragment() : BaseFragment<DashboardViewMvcImpl, DashboardController>() {

    @Inject
    lateinit var settings: Settings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        val pagerAdapter = context?.let { DashboardPagerAdapter(it, childFragmentManager) }
        view = pagerAdapter?.let {
            DashboardViewMvcImpl(inflater, container, childFragmentManager,
                it, DashboardPagerAdapter.TABS_COUNT)
        }
        controller = DashboardController(view)
        val tabId = arguments?.get("tabId") as Int?
        controller?.onCreate(tabId)

        return view?.rootView
    }
}
