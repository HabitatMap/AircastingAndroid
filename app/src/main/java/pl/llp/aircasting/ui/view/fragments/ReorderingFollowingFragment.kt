package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.screens.dashboard.reordering_following.ReorderingFollowingViewMvcImpl

// ReorderingFollowingFragment is the only fragment attached to ReorderingDashboard
class ReorderingFollowingFragment : FollowingFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .userDependentComponent?.inject(this)

        val reorderView = ReorderingFollowingViewMvcImpl(
            layoutInflater,
            null,
            childFragmentManager,
        )
        controller = controllerFactory.create(
            activity,
            reorderView,
            viewLifecycleOwner,
            childFragmentManager,
            context
        )
        reorderView.apply {
            setSessionDismissCallback {
                controller?.sessionDismissCallback(it)
            }
            setSessionUpdateFollowedAtCallback {
                controller?.sessionUpdateFollowedAtCallback(it)
            }
            initializeAdapter {
                controller?.getReloadedSession(it)
            }
        }
        view = reorderView

        if (sessionsRequested) {
            controller?.onCreate()
            sessionsRequested = false
        }

        return view?.rootView
    }

}
