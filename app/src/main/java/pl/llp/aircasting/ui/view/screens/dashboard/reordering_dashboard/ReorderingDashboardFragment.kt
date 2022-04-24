package pl.llp.aircasting.ui.view.screens.dashboard.reordering_dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.ui.view.screens.common.BaseFragment
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardViewMvcImpl

// Reordering dashboard is the screen we enter after pressing "Reorder" button
// It's needed because we want to be able to turn on/ turn off Reorder feature and we need to change number of visible cards depending on it
class ReorderingDashboardFragment : BaseFragment<DashboardViewMvcImpl, ReorderingDashboardController>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val pagerAdapter = context?.let { ReorderingDashboardPagerAdapter(it, childFragmentManager) }
        view = pagerAdapter?.let {
            DashboardViewMvcImpl(inflater, container, childFragmentManager,
                it, ReorderingDashboardPagerAdapter.TABS_COUNT)
        }

        controller = ReorderingDashboardController(view)

        val tabId = arguments?.get("tabId") as Int?
        controller?.onCreate(tabId)

        return view?.rootView
    }
}
