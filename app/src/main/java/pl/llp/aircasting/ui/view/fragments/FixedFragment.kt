package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.screens.dashboard.fixed.FixedController
import pl.llp.aircasting.ui.view.screens.dashboard.fixed.FixedControllerFactory
import pl.llp.aircasting.ui.view.screens.dashboard.fixed.FixedSessionViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.fixed.FixedViewMvcImpl
import javax.inject.Inject


class FixedFragment : Fragment() {
    private lateinit var controller: FixedController
    private lateinit var view: FixedViewMvcImpl<FixedSessionViewMvc.Listener>

    @Inject
    lateinit var controllerFactory: FixedControllerFactory

    private var sessionsRequested = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .userDependentComponent
            .inject(this)

        view = FixedViewMvcImpl(
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

        view.apply {
            initializeAdapter(controller::getReloadedSession)
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

    override fun onDestroy() {
        super.onDestroy()
        controller?.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        controller?.onDestroy()
    }
}
