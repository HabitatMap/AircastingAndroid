package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.ui.view.screens.dashboard.following.FollowingController
import pl.llp.aircasting.ui.view.screens.dashboard.following.FollowingViewMvcImpl
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import javax.inject.Inject


open class FollowingFragment : Fragment() {
    protected var controller: FollowingController? = null
    protected val sessionsViewModel by activityViewModels<SessionsViewModel>()
    protected var view: FollowingViewMvcImpl? = null

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    protected var sessionsRequested = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        view = FollowingViewMvcImpl(
            layoutInflater,
            null,
            childFragmentManager
        )

        controller = FollowingController(
            activity,
            view,
            sessionsViewModel,
            viewLifecycleOwner,
            settings,
            apiServiceFactory,
            context
        )

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
        view = null
        controller?.onDestroy()
        controller = null
    }

    override fun onDestroy() {
        super.onDestroy()
        view = null
        controller?.onDestroy()
        controller = null
    }
}