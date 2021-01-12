package io.lunarlogic.aircasting.screens.dashboard.fixed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import javax.inject.Inject


class FixedFragment : Fragment() {
    private var controller: FixedController? = null
    private val sessionsViewModel by activityViewModels<SessionsViewModel>()

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

        val view = FixedViewMvcImpl(
            layoutInflater,
            null,
            childFragmentManager
        )
        controller = FixedController(
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

        return view.rootView
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
}
