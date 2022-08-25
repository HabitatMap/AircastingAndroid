package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.ui.view.screens.dashboard.active.MobileActiveController
import pl.llp.aircasting.ui.view.screens.dashboard.active.MobileActiveViewMvcImpl
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector
import javax.inject.Inject


class MobileActiveFragment : Fragment() {
    private var controller: MobileActiveController? = null
    private val sessionsViewModel by activityViewModels<SessionsViewModel>()
    private var view: MobileActiveViewMvcImpl? = null

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var airbeamReconnector: AirBeamReconnector

    @Inject
    lateinit var permissionsManager: PermissionsManager

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    private var sessionsRequested = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        view = MobileActiveViewMvcImpl(
            layoutInflater,
            null,
            childFragmentManager
        )
        controller = MobileActiveController(
            activity,
            view,
            sessionsViewModel,
            viewLifecycleOwner,
            settings,
            apiServiceFactory,
            airbeamReconnector,
            permissionsManager,
            requireContext()
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
        controller?.onDestroy()
        controller = null
        view = null
    }

    override fun onDestroy() {
        super.onDestroy()
        controller?.onDestroy()
        controller = null
        view = null
    }
}
