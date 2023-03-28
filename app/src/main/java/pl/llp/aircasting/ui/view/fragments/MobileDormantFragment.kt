package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.screens.dashboard.dormant.MobileDormantController
import pl.llp.aircasting.ui.view.screens.dashboard.dormant.MobileDormantControllerFactory
import pl.llp.aircasting.ui.view.screens.dashboard.dormant.MobileDormantViewMvcImpl
import javax.inject.Inject


class MobileDormantFragment : Fragment() {
    private lateinit var controller: MobileDormantController
    private lateinit var view: MobileDormantViewMvcImpl

    @Inject
    lateinit var controllerFactory: MobileDormantControllerFactory

    private var sessionsRequested = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .userDependentComponent.inject(this)

        view = MobileDormantViewMvcImpl(
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

        view?.initializeAdapter(controller::getReloadedSession)

        if (sessionsRequested) {
            controller.onCreate()
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
        controller.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        controller.onDestroy()
    }
}
