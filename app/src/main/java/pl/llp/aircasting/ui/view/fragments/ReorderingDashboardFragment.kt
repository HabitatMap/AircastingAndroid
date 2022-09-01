package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardViewMvcImpl
import pl.llp.aircasting.ui.view.screens.dashboard.reordering_dashboard.ReorderingDashboardController
import pl.llp.aircasting.ui.view.screens.dashboard.reordering_dashboard.ReorderingDashboardPagerAdapter

class ReorderingDashboardFragment :
    BaseFragment<DashboardViewMvcImpl, ReorderingDashboardController>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val pagerAdapter = ReorderingDashboardPagerAdapter(childFragmentManager)
        view = DashboardViewMvcImpl(
            requireActivity(),
            inflater, container, childFragmentManager,
            pagerAdapter, ReorderingDashboardPagerAdapter.TABS_COUNT
        )

        controller = ReorderingDashboardController(view)

        val tabId = arguments?.get("tabId") as Int?
        controller?.onCreate(tabId)

        return view?.rootView
    }
}
