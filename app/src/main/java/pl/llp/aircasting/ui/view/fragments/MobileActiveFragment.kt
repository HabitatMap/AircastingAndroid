package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.screens.dashboard.active.MobileActiveController
import pl.llp.aircasting.ui.view.screens.dashboard.active.MobileActiveControllerFactory
import pl.llp.aircasting.ui.view.screens.dashboard.active.MobileActiveViewMvcImpl
import javax.inject.Inject


class MobileActiveFragment : Fragment() {
    private lateinit var controller: MobileActiveController
    private lateinit var view: MobileActiveViewMvcImpl

    @Inject
    lateinit var controllerFactory: MobileActiveControllerFactory

    private var sessionsRequested = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .userDependentComponent?.inject(this)

        view = MobileActiveViewMvcImpl(
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

    override fun onDestroyView() {
        super.onDestroyView()
        controller?.onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        controller?.onDestroy()
    }
}
