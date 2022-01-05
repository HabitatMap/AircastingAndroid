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

    companion object {
        fun newInstance(): DashboardFragment {
//            val args: Bundle = Bundle()
//            args.putBoolean("isReordering", isReordering)
            val newFragment = DashboardFragment()
//            newFragment.arguments = args
            return newFragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        view = DashboardViewMvcImpl(inflater, container, childFragmentManager, settings.isReordering())
        controller = DashboardController(view)

        val tabId = arguments?.get("tabId") as Int?
        controller?.onCreate(tabId)

        return view?.rootView
    }
}
