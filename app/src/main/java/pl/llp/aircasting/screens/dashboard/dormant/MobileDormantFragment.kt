package pl.llp.aircasting.screens.dashboard.dormant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.lib.AppBar
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.models.SessionsViewModel
import pl.llp.aircasting.networking.services.ApiServiceFactory
import javax.inject.Inject


class MobileDormantFragment : Fragment() {
    private var controller: MobileDormantController? = null
    private val sessionsViewModel by activityViewModels<SessionsViewModel>()
    private var view: MobileDormantViewMvcImpl? = null

    @Inject
    lateinit var settings: Settings

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

        view = MobileDormantViewMvcImpl(
            layoutInflater,
            null,
            childFragmentManager
        )

        controller = MobileDormantController(
            activity,
            view,
            sessionsViewModel,
            viewLifecycleOwner,
            settings,
            apiServiceFactory,
            childFragmentManager,
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

    override fun onDestroy() {
        super.onDestroy()
        view = null
        controller?.onDestroy()
        controller = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view = null
        controller?.onDestroy()
        controller = null
    }
}
