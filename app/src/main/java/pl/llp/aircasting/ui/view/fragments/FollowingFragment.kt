package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.screens.dashboard.following.FollowingController
import pl.llp.aircasting.ui.view.screens.dashboard.following.FollowingControllerFactory
import pl.llp.aircasting.ui.view.screens.dashboard.following.FollowingViewMvcImpl
import javax.inject.Inject


open class FollowingFragment : Fragment() {
    protected var controller: FollowingController? = null
    protected var view: FollowingViewMvcImpl? = null

    @Inject
    lateinit var controllerFactory: FollowingControllerFactory

    protected var sessionsRequested = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .userDependentComponent?.inject(this)

        view = FollowingViewMvcImpl(
            layoutInflater,
            null,
            childFragmentManager
        )

        controller = controllerFactory.create(
            activity,
            view,
            viewLifecycleOwner,
            childFragmentManager,
            context
        )

        view?.initializeAdapter {
            controller?.getReloadedSession(it)
        }

        if (sessionsRequested) {
            controller?.onCreate()
            sessionsRequested = false
        }

        return view?.rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionsRequested = true
    }

    override fun onResume() {
        super.onResume()
        controller?.onResume()
    }

    override fun onPause() {
        super.onPause()
        controller?.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        controller?.onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        controller?.onDestroy()
    }
}
