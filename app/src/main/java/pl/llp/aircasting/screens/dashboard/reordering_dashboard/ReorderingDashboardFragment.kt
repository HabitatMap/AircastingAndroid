package pl.llp.aircasting.screens.dashboard.reordering_dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
<<<<<<< HEAD
import pl.llp.aircasting.screens.common.BaseFragment
import pl.llp.aircasting.screens.dashboard.DashboardViewMvcImpl

class ReorderingDashboardFragment : BaseFragment<DashboardViewMvcImpl, ReorderingDashboardController>() {
=======
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.screens.common.BaseFragment

class ReorderingDashboardFragment() : BaseFragment<ReorderingDashboardViewMvcImpl, ReorderingDashboardController>() {
>>>>>>> 6c94fa1c (first operational version)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
<<<<<<< HEAD
        val pagerAdapter = context?.let { ReorderingDashboardPagerAdapter(it, childFragmentManager) }
        view = pagerAdapter?.let {
            DashboardViewMvcImpl(inflater, container, childFragmentManager,
                it, ReorderingDashboardPagerAdapter.TABS_COUNT)
        }
        controller = ReorderingDashboardController(view)
=======
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        view = ReorderingDashboardViewMvcImpl(inflater, container, childFragmentManager)
        controller = ReorderingDashboardController(view)

>>>>>>> 6c94fa1c (first operational version)
        val tabId = arguments?.get("tabId") as Int?
        controller?.onCreate(tabId)

        return view?.rootView
    }
}
