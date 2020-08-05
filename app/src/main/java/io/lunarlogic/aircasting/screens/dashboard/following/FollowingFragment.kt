package io.lunarlogic.aircasting.screens.dashboard.following

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


class FollowingFragment : Fragment() {
    private lateinit var controller: FollowingController
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

        val view = FollowingViewMvcImpl(
            layoutInflater,
            null,
            requireContext(),
            childFragmentManager
        )
        controller = FollowingController(
            context,
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
