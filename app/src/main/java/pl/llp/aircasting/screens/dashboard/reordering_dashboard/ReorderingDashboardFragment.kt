package pl.llp.aircasting.screens.dashboard.reordering_dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.screens.common.BaseFragment

class ReorderingDashboardFragment() : BaseFragment<ReorderingDashboardViewMvcImpl, ReorderingDashboardController>() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        view = ReorderingDashboardViewMvcImpl(inflater, container, childFragmentManager)
        controller = ReorderingDashboardController(view)

        val tabId = arguments?.get("tabId") as Int?
        controller?.onCreate(tabId)

        return view?.rootView
    }
}
