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

class DashboardFragment : BaseFragment<DashboardViewMvcImpl, DashboardController>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = DashboardViewMvcImpl(inflater, container, childFragmentManager)
        controller = DashboardController(view)

        val tabId = arguments?.get("tabId") as Int?
        controller?.onCreate(tabId)

        return view?.rootView
    }
}
