package pl.llp.aircasting.screens.dashboard.reordering_following

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.screens.dashboard.following.FollowingController
import pl.llp.aircasting.screens.dashboard.following.FollowingFragment

// ReorderingFollowingFragment is the only fragment attached to ReorderingDashboard
class ReorderingFollowingFragment: FollowingFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        view = ReorderingFollowingViewMvcImpl(
            layoutInflater,
            null,
            childFragmentManager
        )

        controller = context?.let {
            FollowingController(
                activity,
                view,
                sessionsViewModel,
                viewLifecycleOwner,
                settings,
                apiServiceFactory,
                it
            )
        }

        if (sessionsRequested) {
            controller?.onCreate()
            sessionsRequested = false
        }

        return view?.rootView
    }

}
