package io.lunarlogic.aircasting.screens.dashboard.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.Settings
import javax.inject.Inject
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewModel


class MobileActiveFragment : Fragment() {
    private lateinit var controller: MobileActiveController
    private val sessionsViewModel by activityViewModels<SessionsViewModel>()

    @Inject
    lateinit var settings: Settings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        val view = MobileActiveViewMvcImpl(
            layoutInflater,
            null,
            childFragmentManager
        )
        controller = MobileActiveController(
            activity,
            view,
            sessionsViewModel,
            viewLifecycleOwner,
            settings
        )
        controller.onCreate()

        return view.rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        controller.onDestroy()
    }
}
